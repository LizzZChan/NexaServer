package com.hcifuture.nuixserver.nuix.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Serializable
data class Slot(
  val name: String,
  val type: SlotType,
  val defaultValue: String,
  val required: Boolean,
  val description: String,
  var value: String?=null)

enum class SlotType(val description: String) {
  STRING("String"), INT("Int"), TIME("HH:MM"), BOOLEAN("boolean"), DAY("YYYY-MM-DD"), DAYTIME("YYYY-MM-DD HH:MM")
}

fun timeParser(datetimeStr: String): Long {
  if (datetimeStr == "" || datetimeStr == "null") return 0
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  // 将字符串转换为LocalDateTime对象
  val localDateTime = LocalDateTime.parse(datetimeStr, formatter)

  // 将LocalDateTime对象转换为时间戳（以毫秒为单位）
  val timestamp = localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()

  println("timeParser: ${datetimeStr} to ${timestamp}")

  return timestamp

}

fun convertTimeStrToInt(timeStr: String?): Pair<Int, Int>? {
  if (timeStr == null) return null

  val parts = timeStr.split(":")
  return if (parts.size == 2) {
    val hour = parts[0].toIntOrNull()
    val minute = parts[1].toIntOrNull()
    if (hour != null && minute != null) {
      Pair(hour, minute)
    } else {
      null
    }
  } else {
    null
  }
}
