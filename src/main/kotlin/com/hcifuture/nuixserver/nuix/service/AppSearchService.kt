package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.stream.consumeAsFlow

class AppSearchService() : TextService() {
  override val name = "AppSearchService"
  override val description = "app搜索，包括淘宝，京东，拼多多"
  override var displayText: String = "App搜索"
  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("淘宝", "京东", "拼多多")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }
  override var slots: List<Slot> = listOf(
    Slot("content", SlotType.STRING, "iphone 15 pro",true, "想要搜索的商品")
  )

//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:shopping"))
//  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val searchContent = getValueFromSlotNameOrDefault("content", name)
    return listOf(SEARCH_SERVICE_ID_TAOBAO, SEARCH_SERVICE_ID_BROWSER, SEARCH_SERVICE_ID_WEIXIN, SEARCH_SERVICE_ID_XIAOHONGSHU)
      .mapIndexed { index, id ->
        createSearchRPAResponse("“$searchContent”", id, searchContent, displayText, index)
      }.asFlow()
//    val res = createSearchRPAResponse("“$searchContent”", SEARCH_SERVICE_ID_TAOBAO, searchContent, displayText)
//    println("result::::::${res}")
//    return flowOf(res)
  }

}
