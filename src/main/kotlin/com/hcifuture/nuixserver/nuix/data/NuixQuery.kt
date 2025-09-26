package com.hcifuture.nuixserver.nuix.data

import java.io.File

enum class CircleType{
  TEXT,
  IMAGE
}

enum class InputMode(private val value: String) {
  TEXT("textInput"),
  VOICE("voiceInput"),
  RING_VOICE("ringVoiceInput");
  companion object {
    fun fromString(str: String?): InputMode {
      return enumValues<InputMode>().firstOrNull { it.value == str } ?: TEXT
    }
  }
}

data class NuixQuery (
  var instruction: String? = null,
  val inputMode: InputMode = InputMode.TEXT,
  var gesture: String? = null,
  val audio: File? = null,
  var audioText: String? = null,
  var circleText: String? = null,
  var circleType: CircleType = CircleType.TEXT,
  var action: NuixAction? = null,
  val response: MutableList<NuixResponse> = mutableListOf(),
  val lat: Double?= null,
  val lng: Double?= null,
)

data class NuixAction(
  var serviceId: Int? = null,
  var action: String? = null,
)

