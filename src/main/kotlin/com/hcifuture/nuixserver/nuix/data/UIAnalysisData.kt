package com.hcifuture.nuixserver.nuix.data

import kotlinx.serialization.Serializable


@Serializable
class UIAnalysisData {
  var data: String = ""
  var contactName: String = ""
  var inputText : String = ""
  var isGroup: Boolean = false
  var messages: List<MessageData> = listOf()
  var packageName: String = ""
  var page: String = ""
  var version: String = ""
}

@Serializable
class MessageData {
  var time: String = ""
  var isSelf: Boolean = false
  var source: String = ""
  var msgType: String = "text"
  var content: String = ""
}
