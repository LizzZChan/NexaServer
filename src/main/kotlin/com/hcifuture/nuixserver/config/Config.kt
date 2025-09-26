package com.hcifuture.nuixserver.config

data class OpenAI (
  val apiKey: String,
  val proxy: String? = null,
  val baseUrl: String? = null
)

data class Kimi (
  val apiKey: String,
  val server: String,
)

data class BAS(
  val host: String,
  val port: Int
)

data class Config (
  val openai: OpenAI,
  val kimi: Kimi,
  val bas: BAS
)
