package com.hcifuture.nuixserver.nuix.service.base

import com.aallam.openai.api.chat.ChatMessage
import com.hcifuture.nuixserver.nuix.ChatSession
import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.data.NuixQuery
import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.model.ChatModel
import kotlinx.coroutines.flow.*
import java.util.*

abstract class TextService() : NuixService() {

  val chatSession = ChatSession(ChatModel("gpt-3.5-turbo"))


  suspend fun query(prompt: NuixPrompt, model: ChatModel? = null): NuixResponse {
    return createTextResponse(data = chatSession.query(prompt, model).content ?: "null")
  }

  fun queryFlow(prompt: NuixPrompt, model: ChatModel? = null, retJson : Boolean = false, displayText : String = this.displayText): Flow<NuixResponse> {
    return chatSession.queryFlow(prompt, model, retJson).map {
      createTextResponse(it, displayText)
    }
  }


  private fun getQuery(
    query: NuixQuery?,
  ): List<String> {
    return listOfNotNull(
      query?.instruction,
      query?.audioText,
      query?.circleText,
    )
  }

  open fun ruleBasedGuardCircleText(state: NuixState): Boolean {return false}
  open fun ruleBasedGuardInstruction(state: NuixState): Boolean {return false} // instruction and audioText
  open fun ruleBasedGuardScreen(state: NuixState): Boolean {return false} //screenText and packageName


  override suspend fun updateContext(
    state: NuixState,
  ) { }

  override fun ruleBasedGuard(
    state: NuixState,
  ): Boolean {
    val screenGuard = if (state.getScreenGuardOrNot()) {
      ruleBasedGuardScreen(state)
    } else {
      false
    }
    val queryGuard = getQuery(state.query).any { queryType ->
      when (queryType) {
        state.query?.instruction -> ruleBasedGuardInstruction(state)
        state.query?.audioText -> ruleBasedGuardInstruction(state)
        state.query?.circleText -> ruleBasedGuardCircleText(state)
        else -> false
      }
    }
    return screenGuard || queryGuard
  }

//  abstract suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse>
//  abstract suspend fun processInstructionFlow(instruction: String, state: NuixState): Flow<NuixResponse>

//  override suspend fun processQuery(
//    state: NuixState,
//  ): List<NuixResponse> {
//    return getInstructions(state.query)
//      .flatMap { processInstruction(it, state) }
//  }
//
//  override suspend fun processQueryFlow(
//    state: NuixState,
//  ): Flow<NuixResponse> {
//    return getInstructions(state.query).map {
//      processInstructionFlow(it, state)
//    }.merge()
//  }
}
