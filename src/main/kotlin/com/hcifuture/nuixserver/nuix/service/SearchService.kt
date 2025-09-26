package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.data.SlotType
import com.hcifuture.nuixserver.nuix.service.base.TextService
import com.hcifuture.nuixserver.nuix.model.ChatModel
import kotlinx.coroutines.flow.*

class SearchService() : TextService() {
  override val name = "SearchService"
  override val description = "在线搜索,主要是新闻事件或者知识获取"
  val model = ChatModel(model = "kimi")
  override var slots: List<Slot> = listOf(
    Slot("content", SlotType.STRING, "龙舟调", true, "想要搜索的内容")
  )
  override var displayText: String = "在线搜索"



  private val prompt = NuixPrompt("""
    根据用户圈选的内容以及其他上下文信息（包括界面信息、用户指令）线上搜索相关信息。
    用户圈选的内容：#{circleText}
    界面信息：#{screen}
    用户指令：#{instruction}
    直接返回搜索结果，不要超过30个字。
  """)

  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("搜索","搜一下")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }


//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:search"))
//  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    prompt.reset()
      .fill("instruction", state.query?.instruction)
      .fill("screen", state.currentContext.screenDescription)
      .fill("circleText", state.query?.circleText)
    displayText = getValueFromSlotName("content")?.let {
      "搜索${it}"
    } ?: name
    return queryFlow(
      prompt = prompt,
      model = model,
      displayText = displayText
    )
  }
}

