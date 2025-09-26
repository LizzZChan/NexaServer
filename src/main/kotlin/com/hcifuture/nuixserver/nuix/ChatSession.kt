package com.hcifuture.nuixserver.nuix

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.hcifuture.nuixserver.nuix.model.ChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatSession(val chatModel: ChatModel) {
  private val messages: MutableList<ChatMessage> = mutableListOf()

  fun lastMessage(): ChatMessage? = messages.lastOrNull()

  fun addMessage(message: ChatMessage){
    messages.add(message)
  }

  suspend fun query(
    prompt: NuixPrompt,
    model: ChatModel? = null,
    retJson: Boolean = false
  ): ChatMessage {
    messages.add(
      ChatMessage(
        role = ChatRole.User,
        content = prompt.prompt,
      )
    )
    val chatCompletion = model?.query(messages, retJson) ?: chatModel.query(messages, retJson)
    val systemMessage = chatCompletion.choices[0].message
    messages.add(systemMessage)
    return systemMessage
  }

  fun queryFlow(
    prompt: NuixPrompt,
    model: ChatModel? = null,
    retJson: Boolean = false
  ): Flow<String> {
    messages.add(
      ChatMessage(
        role = ChatRole.User,
        content = prompt.prompt,
      )
    )
    val flow = model?.queryFlow(messages, retJson) ?: chatModel.queryFlow(messages)
    CoroutineScope(Dispatchers.Default).launch {
      var content = ""
      flow.collect {
        content += it
      }
      messages.add(
        ChatMessage(
          role = ChatRole.System,
          content = content,
        )
      )
    }
    return flow.map {
      it.choices[0].delta?.content ?: ""
    }
  }
}
