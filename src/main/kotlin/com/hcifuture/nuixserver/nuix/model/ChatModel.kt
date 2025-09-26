package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.config.Config
import com.hcifuture.nuixserver.di.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance

class ChatModel(
  private val model: String = "gpt-3.5-turbo",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance(if (model == "kimi") "kimi" else "openai")

  suspend fun query(messages: List<ChatMessage>, retJson: Boolean = false): ChatCompletion {
    val format = if (retJson) ChatResponseFormat.JsonObject else ChatResponseFormat.Text
    return openAI.chatCompletion(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
        responseFormat = format
      )
    )
  }

  fun queryFlow(messages: List<ChatMessage>, retJson: Boolean = false): Flow<ChatCompletionChunk> {
    val format = if (retJson) ChatResponseFormat.JsonObject else ChatResponseFormat.Text
    println("???????: invoke time：$messages")
    return openAI.chatCompletions(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
        responseFormat = format
      ),
    )
  }
}
