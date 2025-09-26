package com.hcifuture.nuixserver.nuix

import com.aallam.openai.api.chat.TextContent
import com.hcifuture.nuixserver.auth.JWTAuthProc
import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.model.AudioModel
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.model.VisionModel
import com.hcifuture.nuixserver.nuix.selector.LLMSelector
import com.hcifuture.nuixserver.nuix.selector.RuleSelector
import com.hcifuture.nuixserver.session.ui.ElementNodeBounds
import com.hcifuture.nuixserver.session.ui.PathPoint
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.imageio.ImageIO

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject


class NuixSession(
  val id: String,
  val name: String,
  val memory: NuixMemory,
) {
  private val scheduler: NuixScheduler = NuixScheduler()
  private var state: NuixState = NuixState(
    currentContext = NuixContext.empty(),
    queryHistory = mutableListOf(),
    contextHistory = mutableListOf(),
    memory = memory,
    services = mutableListOf(),
    serviceHistory = mutableListOf()
  )

  val logger: Logger = LoggerFactory.getLogger(JWTAuthProc::class.java)
  private val audioModel = AudioModel()

  suspend fun updateContext(context: NuixContext): List<String> {
    state.contextHistory.add(context)
    state.currentContext = state.currentContext.update(context)
    state.currentContext = state.currentContext.update(NuixContext(screenDescription = describeScreen(state)))
    scheduler.updateContext(state.setContext(context))
    return listOf("Done")
  }

  private suspend fun describeScreen(state: NuixState): String {
    val chatSession: ChatSession = ChatSession(ChatModel("gpt-4o"))
    val describeScreenPrompt: NuixPrompt = NuixPrompt(
      """
        ###
        手机界面上的文字信息包括：#{treeText}
        ###

        用一句话描述当前的界面内容，不超过50个字。如果包含非中文的信息，需要特别关注，并进行说明。
    """
    )
    val prompt = describeScreenPrompt
      .reset()
      .fill("treeText", state.currentContext.uiTree?.treeText?.joinToString(separator = " "))
    return chatSession.query(prompt = prompt).content?.trim() ?: "null"
  }

  private suspend fun extractQuery(query: NuixQuery) {
    if (query.instruction ==""){
      query.instruction = null
    }
    if (query.gesture == ""){
      query.gesture = null
    }
    if (query.audio != null && query.audioText == null) {
      query.audioText = audioModel.asr(
        name = "audio.mp4",
        source = FileSystem.SYSTEM.source(query.audio.absolutePath.toPath())
      ).text
      query.instruction = query.audioText
    }
    if (!query.gesture.isNullOrEmpty() && query.circleText == null && state.currentContext.uiTree != null) {
      val points: List<PathPoint> = query.gesture!!
        .split(',')
        .map { s -> s.toFloat() }
        .chunked(3)
        .map { point ->
          PathPoint(t = point[0], x = point[1], point[2])
        }
      val circleTextFromUiTree = state.currentContext.uiTree!!.extractText(
        ElementNodeBounds.fromPoints(points)
      )

      val circleTextFromImage = state.currentContext.screenshot?.let {
        val croppedImageFile = cropImage(state.currentContext.screenshot!!, ElementNodeBounds.fromPoints(points))
        state.currentContext.circledImage = croppedImageFile
        logger.info("croppedImage: $croppedImageFile")
        extractTextFromImage(croppedImageFile!!, circleTextFromUiTree.joinToString(separator = " "), query)
      }

      println("circleTextFromUiTree:$circleTextFromUiTree; circleTextFromImage:$circleTextFromImage, query.circleText: ${query.circleText}")

      state.currentContext.currentLocation = "北京市海淀区"
    }
  }

  private suspend fun extractTextFromImage(imageFile: File, circleTextFromUiTree:String, query: NuixQuery){
    val visionModel = VisionModel()
    val result = visionModel.performOcr(imageFile, circleTextFromUiTree).choices[0].message.messageContent?.let {
      (it as TextContent).content
    }
    println("ocrResponse: $result")
    val jsonObject = JSONObject(result)
    val circleType = jsonObject.getString("type")
    if (circleType == "IMAGE") query.circleType = CircleType.IMAGE
    val circleContent = jsonObject.getString("content")
    query.circleText = circleContent
  }


  private fun cropImage(file: File, bounds: ElementNodeBounds): File? {
    if (!file.exists()) return null

    val originalImage = ImageIO.read(file)
    val croppedImage = originalImage.getSubimage(bounds.left, bounds.top, bounds.right - bounds.left, bounds.bottom - bounds.top)

    val fileNameWithoutExtension = file.nameWithoutExtension
    val fileExtension = file.extension
    val newFileName = "${fileNameWithoutExtension}_crop.png"
    val newFile = File("cropped", newFileName)
//    val newFile = File(file.parent, newFileName)

    ImageIO.write(croppedImage, "png", newFile)
    return newFile
  }

  suspend fun executeServiceFlow(messageId: String): Flow<NuixResponse> {
    return scheduler.executeService(messageId, state)
  }

  suspend fun requestRemoteServiceFlow(messageId: String, info: JsonObject): Flow<NuixResponse> {
    return scheduler.requestRemoteService(messageId, state, info)
  }


  suspend fun processQueryFlow(query: NuixQuery): Flow<NuixResponse> {
    extractQuery(query)
    state.queryHistory.add(query)
    state = state.setQuery(query)
    return scheduler.processQueryFlow(state)
  }

  suspend fun processQuery(query: NuixQuery): List<NuixResponse> {
    extractQuery(query)
    state.queryHistory.add(query)
    return scheduler.processQuery(state.setQuery(query))
  }
}
