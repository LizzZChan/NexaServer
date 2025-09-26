package com.hcifuture.nuixserver.session

import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.model.ResponseMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatSession(val chatModel: ChatModel) {
  val messages: MutableList<ChatMessage> = mutableListOf()

  fun lastMessage(): ChatMessage? = messages.lastOrNull()

  fun addAssistantMessage(message: String){
    messages.add(
      ChatMessage(
        role = ChatRole.Assistant,
        content = message,
      )
    )
  }

  suspend fun query(
    prompt: String,
    model: ChatModel? = null,
  ): ChatMessage {
    messages.add(
      ChatMessage(
        role = ChatRole.User,
        content = prompt,
      )
    )
    val chatCompletion = model?.query(messages) ?: chatModel.query(messages)
    val systemMessage = chatCompletion.choices[0].message
    messages.add(systemMessage)
    return systemMessage
  }

  suspend fun queryFlow(prompt: String, retJson: Boolean = false): Flow<String> {
    messages.add(
      ChatMessage(
        role = ChatRole.User,
        content = prompt,
      )
    )
    return chatModel.queryFlow(messages, retJson).map {
      it.choices[0].delta?.content ?: ""
    }

  }
}
