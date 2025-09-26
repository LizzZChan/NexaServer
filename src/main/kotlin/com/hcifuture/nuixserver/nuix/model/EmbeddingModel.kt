package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.embedding.EmbeddingResponse
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.di.Inject
import org.kodein.di.DIAware
import org.kodein.di.instance

class EmbeddingModel(
  private val model: String = "text-embedding-3-small",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance("openai")

  suspend fun embedding(input: List<String>): EmbeddingResponse {
    return openAI.embeddings(
      request = EmbeddingRequest(
        model = ModelId(model),
        input = input,
      )
    )
  }
}
