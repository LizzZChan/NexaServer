package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import java.util.*

class ChatService() : TextService() {
  override val name = "ChatService"
  override val description = "聊天机器人"
  override var slots: List<Slot> = listOf()
  override var displayText: String = "聊天机器人"

  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("你好")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }

  private val prompt = NuixPrompt("""
    根据用户输入并结合其他上下文信息（交互记录、用户在界面上选中的信息界面信息）回复消息。
    用户输入: #{instruction}
    之前的交互记录为: #{history}
    界面信息：#{screen}
    用户在界面上选中的信息：#{circleText}
    直接返回回复内容，不要超过50个字。
  """)

//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:chat"))
//  }

  override suspend fun processQueryFlow( state: NuixState): Flow<NuixResponse> {
    val prompt = prompt.reset()
      .fill("instruction", state.query?.instruction)
      .fill("history", state.getHistory(state))
      .fill("screen", state.currentContext.uiTree?.treeText?.joinToString(separator = " "))
      .fill("circleText", state.query?.circleText)
    return queryFlow(prompt = prompt)
  }


}
