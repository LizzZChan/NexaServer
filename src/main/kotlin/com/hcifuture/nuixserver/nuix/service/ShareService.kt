package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.data.SlotType
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ShareService() : TextService() {
  override val name = "ShareService"
  override val description = "分享当前界面内容给微信好友，包括购物app看到商品，好物推荐页面或者新闻等。"
  override var displayText: String = "分享"
  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("分享", "发给", "转发", "发送", "传送")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }
  override var slots: List<Slot> = listOf(
    Slot("content", SlotType.STRING, "this page",true, "想要分享的内容"),
    Slot("person", SlotType.STRING, "Liz",true, "想要分享的对象")
  )

//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:shareToFriend"))
//  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    return flowOf(createTextResponse(data = "service:share, $slots"))
  }
}
