package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.data.SlotType
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SummarizeService() : TextService() {
  override val name = "SummarizeService"
  override val description = "总结文本"
  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("总结", "概括")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }
  override var displayText = "总结"
  override var slots: List<Slot> = listOf(
    Slot("content", SlotType.STRING, "一段文本",true, "需要被总结的文本")
  )

  private val prompt = NuixPrompt("""
    用一句话总结这段文本：#{content}
    直接返回总结后的内容。
  """)

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val prompt = prompt.reset()
      .fill("content", getValueFromSlotName("content"))
    println("SUMMARIZE::::: $prompt")
    return queryFlow(prompt = prompt, displayText = displayText)
  }
}
