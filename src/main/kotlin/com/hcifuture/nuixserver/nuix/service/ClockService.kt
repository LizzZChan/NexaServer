package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ClockService: TextService() {
  override val name = "ClockService"
  override val description = "设置闹钟"
  override var slots: List<Slot> = listOf(
    Slot("message", SlotType.STRING, "起床啦",true,"闹钟提示消息" ),
    Slot("time",SlotType.TIME, "09:30", true,"时间"),
    Slot("days",SlotType.STRING, "1", false,"一周内的哪几天提醒(1-7分别表示星期天到星期六)"),
    )
  override var displayText: String = "设置闹钟"


  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("闹钟")
    return keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    displayText = getValueFromSlotName("time")?.let {
      "设置${it}的闹钟"
    }?: name
    val timeStr = getValueFromSlotName("time")
    val time = convertTimeStrToInt(timeStr)
    var hour: Int? = null
    var minute: Int? = null
    if (time != null) {
      hour = time.first
      minute = time.second
    }
    println("timeInClock. timeStr:${timeStr}, time:${time}, hour: ${hour}, minute: ${minute}")

    val clientServiceInfo = NuixClientService().also {
      it.serviceType= 1
      it.serviceId = "-3"
      it.category = 17
      it.params = buildJsonObject {
        put("title", JsonPrimitive(getValueFromSlotNameOrDefault("message", "闹钟")))
        put("hour", JsonPrimitive(hour))
        put("minute", JsonPrimitive(minute))
      }
    }
    return flowOf(createRPAResponse(clientServiceInfo, displayText, "设置闹钟", 0))
  }

}
