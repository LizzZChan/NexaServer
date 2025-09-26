package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarService():TextService() {
  override val name = "CalendarService"
  override val description = "将日程加入日历，例如讲座信息，腾讯会议等相关信息"
  override var slots: List<Slot> = listOf(
    Slot("event", SlotType.STRING, "UIUC大学黄芸教授“Human-AI Synergy: From Research Prototypes to Real-World Impact” 报告",true,"日程的摘要" ),
    Slot("location", SlotType.STRING, "FIT楼1-315",true,"日程地点"),
    Slot("beginTime",SlotType.DAYTIME, "2024-06-17 09:30", true,"开始时间"),
    Slot("endTime",SlotType.DAYTIME, "2024-06-17 11:30", false,"结束时间"),
    //Slot("repeatRule", SlotType.STRING, "Once", false, "提醒频率(Once, Daily, Week, Month)")
  )
  override var displayText: String = "加入日程"


  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("提醒", "加入", "日程", "日历")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }

  override fun ruleBasedGuardScreen(state: NuixState): Boolean {
    val uiTreeKeywords = listOf("腾讯会议", "zoom")
    return uiTreeKeywords.any {
        keyword -> state.currentContext.uiTree?.treeText?.contains(keyword) ?: false
    }
  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    displayText = getValueFromSlotName("event")?.let {
      "将${it}加入日程"
    }?: name

    val beginTimeStr = getValueFromSlotNameOrDefault("beginTime", "")
    var endTimeStr = getValueFromSlotNameOrDefault("endTime", "")
    if (beginTimeStr.isNotEmpty() && endTimeStr.isEmpty()) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val beginTime = LocalDateTime.parse(beginTimeStr, formatter)
        val endTime = beginTime.plusHours(1)
        endTimeStr = endTime.format(formatter)
      }

    val clientServiceInfo = NuixClientService().also {
      it.serviceType= 1
      it.serviceId = "-4"
      it.category = 17
      it.params =  buildJsonObject {
        put("title", JsonPrimitive(getValueFromSlotNameOrDefault("event", "")))
        put("description", JsonPrimitive((state.query?.instruction ?: "")))
        put("beginTime", JsonPrimitive(timeParser(beginTimeStr)))
        put("endTime", JsonPrimitive(timeParser(endTimeStr)))
        put("address", JsonPrimitive(getValueFromSlotNameOrDefault("location", "")))
        put("repeatRule", JsonPrimitive(getValueFromSlotNameOrDefault("repeatRule", "")))
      }
    }
    return flowOf(createRPAResponse(clientServiceInfo, displayText, "添加日程", 0))
  }

}
