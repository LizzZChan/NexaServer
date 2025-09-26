package com.hcifuture.nuixserver.nuix.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.nuix.NuixMemory
import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import com.hcifuture.nuixserver.session.ChatSession
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.model.VisionModel
import com.hcifuture.nuixserver.session.ui.ElementTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.kodein.di.instance

class VisionQAService() : TextService() {
  override val name = "VisionQAService"
  override val description = "针对圈选的图像内容的图片问答器"
  override var slots: List<Slot> = listOf()
  override var displayText: String = "图片问答器"
  override fun ruleBasedGuard(state: NuixState): Boolean {
    return false
  }
  val model = VisionModel()

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val result = model.performQA(state.currentContext.circledImage!!, state).choices[0].message.messageContent?.let {
      (it as TextContent).content
    }
    return flowOf(createTextResponse(result!!))
  }

}
