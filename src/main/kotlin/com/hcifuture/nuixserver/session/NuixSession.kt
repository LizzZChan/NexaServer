package com.hcifuture.nuixserver.session

import com.hcifuture.nuixserver.nuix.model.AudioModel
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.session.ui.ElementNodeBounds
import com.hcifuture.nuixserver.session.ui.ElementTree
import com.hcifuture.nuixserver.session.ui.PathPoint
import com.hcifuture.nuixserver.tool.crawler.Crawler
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File

class NuixSession(
  val userId: String,
  val sessionId: String,
) {
  private val chatSession: ChatSession = ChatSession(ChatModel())
  private val audioModel: AudioModel = AudioModel()
  private var uiTree: ElementTree? = null
  private val crawler: Crawler = Crawler()

  suspend fun queryWithCrawler(
    instruction: String,
  ) {
    var crawlerResult: String = ""
    try {
      crawlerResult = crawler.searchBing(prompt = instruction.replace("\\s".toRegex(), "")).joinToString {
        "Url: ${it.url}\nTitle: ${it.title}\nContent: ${it.content}"
      }
    } catch(_: Throwable) {
    }
    val prompt = "${crawlerResult}\n用户的询问是${instruction}是什么？，以上是搜索引擎的结果，请根据这些内容详细地回答用户问题"
    chatSession.query(
      prompt = prompt
    )
  }

  suspend fun query(
    instruction: String,
  ) {
  }

  suspend fun updateContext(
    uiTree: String?,
    screenshot: String?,
  ): String? {
    uiTree?.let {
      this.uiTree = ElementTree(File(uiTree).readText())
    }
    return null
  }

  suspend fun queryFromWeb(
    instruction: String?,
    gestureStr: String?,
    audio: String?,
  ): String? {
    instruction?.let {
      queryWithCrawler(it)
    }
    val gesture: List<PathPoint>? = gestureStr?.let {
      it.split(',').map { s -> s.toFloat() }
        .chunked(3)
        .map { point ->
          PathPoint(t = point[0], x = point[1], point[2])
        }
    }
    gesture?.let { points ->
      uiTree?.let { uiTree ->
        queryWithCrawler(uiTree.extractText(
            ElementNodeBounds.fromPoints(points)
          ).joinToString("\n")
        )
      }
    }
    audio?.let {
      queryWithCrawler(
        audioModel.asr(
            name = "audio.mp4",
            source = FileSystem.SYSTEM.source(it.toPath())
          ).text
      )
    }
    return chatSession.lastMessage()?.content
  }

  fun close() {

  }
}
