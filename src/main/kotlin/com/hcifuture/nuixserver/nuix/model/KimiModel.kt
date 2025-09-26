package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.di.Inject
import kotlinx.coroutines.flow.Flow
import org.kodein.di.DIAware
import org.kodein.di.instance

class KimiModel(
  private val model: String = "kimi",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance("kimi")

  suspend fun query(messages: List<ChatMessage>): ChatCompletion {
    return openAI.chatCompletion(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
      ),
    )
  }

  fun queryFlow(messages: List<ChatMessage>): Flow<ChatCompletionChunk> {
    return openAI.chatCompletions(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
      ),
    )
  }
}
