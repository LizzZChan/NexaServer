package com.hcifuture.nuixserver.nuix.data

import com.hcifuture.nuixserver.nuix.NuixService

enum class ConversationState {
  INITIAL,
  IN_PROGRESS,
  COMPLETED,
  TRIGGER_LAUNCHER
}


data class NuixConversation(
  var conState: ConversationState?,
  var service: MutableList<NuixService>?,
  var missingSlots: MutableMap<NuixService, List<Slot>>?
){
  companion object {
    fun empty(): NuixConversation {
      return NuixConversation(null, null,null)
    }
  }
}
