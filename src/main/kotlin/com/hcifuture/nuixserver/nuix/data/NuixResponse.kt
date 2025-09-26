package com.hcifuture.nuixserver.nuix.data

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.UUID

val module = SerializersModule {
  polymorphic(NuixResponseData::class) {
    subclass(NuixResponseDataText::class, NuixResponseDataText.serializer())
    subclass(NuixResponseDataImage::class, NuixResponseDataImage.serializer())
    subclass(NuixResponseDataIconItem::class, NuixResponseDataIconItem.serializer())
    subclass(NuixResponseDataForm::class, NuixResponseDataForm.serializer())
  }
  polymorphic(NuixFormItem::class) {
    subclass(NuixFormItemCheckList::class, NuixFormItemCheckList.serializer())
  }
}

enum class MessageType(name: String) {
  TEXT("text"),
  IMAGE("image"),
  ICON_ITEM("iconitem"),
  FORM("form");

  override fun toString(): String {
    return name;
  }
}

enum class MessageState(name: String) {
  BRIEF("brief"),
  PART("part"),
  END("end");
  override fun toString(): String {
    return name;
  }
}

@Serializable
class NuixClientService {
  var serviceType: Int = 1
  var serviceId: String = ""
  var category: Int? = null
  var packageName: String = ""
  var params: JsonObject? = null
  override fun toString(): String {
    return NuixResponse.json.encodeToString(this)
  }
}

@Serializable
class NuixResponse(
  var code: Int = 200,
  var msg: String = "success",
  var data: NuixResponseData
) {
  override fun toString(): String {
    return json.encodeToString(this)
  }

  companion object {
    val json = Json {
      coerceInputValues = true
      encodeDefaults = true
      serializersModule = module
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("messageType")
sealed class NuixResponseData() {
  var messageState: MessageState = MessageState.END
  var messageId : String = ""
  var serviceName: String = ""
  var serviceDisplayText: String = ""
  var itemId: String? = null
  open val messageContent: String
    get() {
      return javaClass.kotlin.members
        .find { it.name == "text" }
        ?.call(this) as? String
        ?: throw NotImplementedError("Subclasses must implement getSpecificValue or have a 'text' property")
    }

}

@Serializable
class NuixResponseDataUserAction {
  var action: String = "copy"
  var text: String = "复制"
}
@Serializable
@SerialName("text")
class NuixResponseDataText : NuixResponseData() {
  var text: String = ""
  var defaultAction: String? = null
  var actions: MutableList<NuixResponseDataUserAction> = mutableListOf()
}

@Serializable
@SerialName("image")
class NuixResponseDataImage(): NuixResponseData() {
  var imageUrl : String? = null
  var imageBase64: String? = null
  var url: String? = null
  var defaultAction: String? = null
  var actions: MutableList<NuixResponseDataUserAction> = mutableListOf()
  override val messageContent: String
    get() = TODO("not implemented")
}

@Serializable
@SerialName("iconitem")
class NuixResponseDataIconItem: NuixResponseData() {
  var imageUrl : String? = null
  var imageBase64: String? = null
  var url: String? = null
  var text: String = ""
  var clientService: NuixClientService? = null
}

@Serializable
@SerialName("form")
class NuixResponseDataForm: NuixResponseData() {
  var form: NuixForm? = null
  override val messageContent: String
    get() = form?.let {NuixResponse.json.encodeToString(form)} ?: "null form"
}

enum class NuixRequestMethod(name: String) {
  POST("post"),
  GET("get")
}
enum class NuixContentType(name: String) {
  JSON("application/json")
}

enum class NuixFormItemType(name: String) {
  CHECK_LIST("checkList")
}
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class NuixFormItem {
  var name: String = ""
  var label: String = ""
  var defaultValue: String? = null
}

@Serializable
class NuixFormItemCheckListOption {
  var key: String = ""
  var label: String = ""
  var desc: String = ""
}
@Serializable
@SerialName("checklist")
class NuixFormItemCheckList: NuixFormItem() {
  var options: MutableList<NuixFormItemCheckListOption> = mutableListOf()
  var maxSelectCount: Int = 0
}

@Serializable
class NuixFormTextButton {
  var text: String? = null
}

@Serializable
class NuixForm {
  var path: String? = null
  var method: NuixRequestMethod = NuixRequestMethod.GET
  var contentType: NuixContentType = NuixContentType.JSON
  var obj: String? = null
  var items: MutableList<NuixFormItem> = mutableListOf()
  var textButton: NuixFormTextButton? = null
}

@Serializable
class NuixResponseDataO {
  var messageType: MessageType = MessageType.TEXT
  var messageState: String = ""
  var messageContent: String = ""
  var messageId: String = UUID.randomUUID().toString()
  var weight: Int = 0
  var picUrl: String? = null
  var url: String? = null
  var serviceName: String = ""
  var serviceDisplayText: String = ""
  var itemId: String = "0"
  var clientService: NuixClientService? = null
  var defaultAction: String? = null

  override fun toString(): String {
    return NuixResponse.json.encodeToString(this)
  }
}



//fun NuixResponse.toJsonObject(): JsonObject {
//  Json.encodeToString()
//  return JsonObject().apply {
//    put("serviceName", serviceName)
//    put("serviceType", serviceType)
//    put("serviceId", serviceId)
//    put("messageType", messageType.toString())
//    put("messageContent", messageContent)
//    put("messageId", messageId)
//    put("messageState", messageState)
//    put("weight", weight)
//    put("picUrl", picUrl)
//    put("extra", extra)
//
//  }
//}
