package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*

class ScanService() : TextService() {
  override val name = "ScanService"
  override val description = "二维码扫码"
  override var slots: List<Slot> = listOf()
  override var displayText: String = "扫码"
  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("二维码", "扫一扫", "扫一下", "扫描", "扫码", "请扫")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }

  override fun ruleBasedGuardScreen(state: NuixState): Boolean {
    val uiTreeKeywords = listOf("二维码")
    return uiTreeKeywords.any { keyword -> state.currentContext.uiTree?.treeText?.contains(keyword) ?: false }
  }


//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:scanQRCode"))
//  }

  override suspend fun processQueryFlow( state: NuixState): Flow<NuixResponse> {
    return flowOf(createTextResponse(data = "service:scanQRCode"))
  }

}
