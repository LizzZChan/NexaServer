package com.hcifuture.nuixserver.nuix

import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.service.base.RemoteService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.reflect.full.primaryConstructor



abstract class NuixService() {
  abstract val name: String
  abstract val description: String
  abstract var slots: List<Slot>
  abstract var displayText: String
  val serviceInstanceId = UUID.randomUUID().toString()

  fun getValueFromSlotNameOrDefault(slotName: String, defValue: String): String {
    return slots.find {
      it.name == slotName
    }?.value ?: defValue
  }

  fun getValueFromSlotName(slotName: String): String? {
    return slots.find {
      it.name == slotName
    }?.value
  }


  fun updateSlots(serviceSlots: JsonObject) {
    slots.forEach { slot ->
      slot.value = (serviceSlots[slot.name]?.jsonPrimitive?.content)?:slot.defaultValue
      //getString(slot.name, slot.defaultValue)
    }
  }


  fun getMissingSlots(): List<Slot> {
      return slots.filter { it.required && (it.value == null || it.value =="null") }
  }

  fun createRPAResponse(
    clientServiceInfo: NuixClientService,
    displayText: String,
    text: String,
    index: Int
  ): NuixResponse {
    return NuixResponse(
      data = NuixResponseDataIconItem().apply {
        messageState = MessageState.END
        this.text = text
        serviceName = name
        serviceDisplayText = displayText
        messageId = serviceInstanceId
        itemId = "$serviceInstanceId.$index"
        clientService = clientServiceInfo
      }
    )
  }

  val SEARCH_SERVICE_ID_BROWSER = "-1"
  val SEARCH_SERVICE_ID_APPMARKET = "-2"
  val SEARCH_SERVICE_ID_XIAOHONGSHU = "279"
  val SEARCH_SERVICE_ID_TAOBAO = "305"
  val SEARCH_SERVICE_ID_WEIXIN = "275"
  val SEARCH_SERVICE_ID_ZHIHU = "304"
  val SEARCH_SERVICE_ID_WEIBO = "298"
  val SEARCH_SERVICE_ID_DAZHONGDIANPING = "293"

  private val serviceId2PackageName = mapOf(
    SEARCH_SERVICE_ID_XIAOHONGSHU to "com.xingin.xhs",
    SEARCH_SERVICE_ID_TAOBAO to "com.taobao.taobao",
    SEARCH_SERVICE_ID_WEIXIN to "com.tencent.mm",
    SEARCH_SERVICE_ID_ZHIHU to "com.zhihu.android",
    SEARCH_SERVICE_ID_WEIBO to "com.sina.weibo",
    SEARCH_SERVICE_ID_DAZHONGDIANPING to "com.dianping.v1"
  )


  fun createSearchRPAResponse(captionText: String, serviceId: String, searchContent: String, displayText: String, index: Int = 0): NuixResponse {
    val clientServiceInfo = NuixClientService().also {
      it.serviceType= 1
      it.serviceId = serviceId
      it.category = 9
      if (serviceId2PackageName.containsKey(serviceId)) {
        it.packageName = serviceId2PackageName[serviceId]!!
      }
      it.params = buildJsonObject {
        put("search_content", JsonPrimitive(searchContent))
      }
    }
    return createRPAResponse(clientServiceInfo, displayText, captionText, index)
  }


  fun createTextResponse(
    data: String,
    displayText: String = this.displayText,
    index: Int? = null,
    isEnd: Boolean = false,
    defaultAction: String = "copy",
    messageState: MessageState = MessageState.PART
  ): NuixResponse {
    return NuixResponse(
      data = NuixResponseDataText().apply {
        serviceName = name
        serviceDisplayText = displayText
        messageId = serviceInstanceId
        itemId = "$serviceInstanceId.$index"
        text = data
        this.defaultAction = defaultAction
        this.messageState = messageState
      })
    }

  open fun createDefaultResponse(
    displayText: String = this.displayText,
    index: Int? = null,
    isEnd: Boolean = false,
    defaultAction: String = "copy"
  ): NuixResponse {
    return NuixResponse(
      data = NuixResponseDataText().apply {
        serviceName = name
        serviceDisplayText = displayText
        messageId = serviceInstanceId
        itemId = "$serviceInstanceId.$index"
        text = ""
        this.defaultAction = defaultAction
        messageState = if (isEnd) MessageState.END else MessageState.BRIEF
      })
  }
//    = NuixResponse(
//      serviceName = description,
//      serviceType = typeID,
//      serviceId = "",
//      messageType = MessageType.TEXT,
//      messageContent = data,
//      messageId = serviceInstanceId,
//      messageState = data,
//      weight = 0,
//      picUrl = ""
//    )

  abstract fun ruleBasedGuard(
    state: NuixState,
  ): Boolean

  abstract suspend fun updateContext(
    state: NuixState,
  )

  suspend fun processQuery(
    state: NuixState,
  ): List<NuixResponse> {
    return emptyList()
  }

  abstract suspend fun processQueryFlow(
    state: NuixState
  ): Flow<NuixResponse>

}

fun createServiceInstance(serviceName: String): NuixService? {
  val fullyQualifiedName = "com.hcifuture.nuixserver.nuix.service.$serviceName"
  return try {
    val kClass = Class.forName(fullyQualifiedName).kotlin
    val constructor = kClass.primaryConstructor
    if (constructor != null && constructor.parameters.isEmpty()) {
      constructor.call() as NuixService
    } else {
      println("No primary constructor found for $fullyQualifiedName")
      null
    }
  } catch (e: ClassNotFoundException) {
    println("Class not found: $fullyQualifiedName")
    if (NuixScheduler.remoteServices.containsKey(serviceName)) {
      println("Create Remote Service: ${NuixScheduler.remoteServices[serviceName]!!}")
      RemoteService(NuixScheduler.remoteServices[serviceName]!!)
    } else null
  } catch (e: Exception) {
    println("Failed to create instance for $fullyQualifiedName: ${e.message}")
    null
  }
}
