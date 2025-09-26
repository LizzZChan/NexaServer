package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.NuixMemory
import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import com.hcifuture.nuixserver.session.ChatSession
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.session.ui.ElementTree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TranslateService() : TextService() {
  override val name = "TranslateService"
  override val description = "翻译器，主要是将其他语言翻译成中文，例如英译中，日译中"
  override var slots: List<Slot> = listOf(
    Slot("content", SlotType.STRING, "English words or paragraph",true, "需要翻译的英文单词或者段落")
    )
  override var displayText: String = "翻译器"
  private val prompt = NuixPrompt("""
    翻译：#{content}
    直接返回翻译后的内容。
  """)

  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("翻译", "英语")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }

//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    val prompt = prompt.reset()
//      .fill("instruction", instruction)
//    return listOf(query(prompt = prompt))
//  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val prompt = prompt.reset()
      .fill("content", getValueFromSlotName("content"))
    println("TRANSLATE::::: $prompt")
    return queryFlow(prompt = prompt, displayText = displayText)
  }
}
