package com.hcifuture.nuixserver.nuix.data

import com.hcifuture.nuixserver.nuix.NuixMemory
import com.hcifuture.nuixserver.nuix.NuixService

data class NuixState (
  var currentContext: NuixContext,
  val contextHistory: MutableList<NuixContext>,
  val query: NuixQuery? = null,
  val queryHistory: MutableList<NuixQuery>,
  var services: MutableList<NuixService>,
  val serviceHistory: MutableList<List<NuixService>>,
  val memory: NuixMemory,
) {
  fun setContext(context: NuixContext): NuixState {
    return NuixState(
      currentContext = currentContext,
      queryHistory = queryHistory,
      contextHistory = contextHistory,
      memory = memory,
      query = null,
      services = services,
      serviceHistory = serviceHistory
    )
  }

  fun setQuery(query: NuixQuery): NuixState {
    return NuixState(
      currentContext = currentContext,
      queryHistory = queryHistory,
      contextHistory = contextHistory,
      memory = memory,
      query = query,
      services = services,
      serviceHistory = serviceHistory
    )
  }

  fun getSecondLastQuery(): NuixQuery? {
    val historySize = queryHistory.size
    return if (historySize >= 2) {
      queryHistory[historySize - 2]
    } else {
      null
    }
  }

  fun isLauncher():Boolean{
    return currentContext.uiTree?.getPackageName() in launchers
  }

  fun isLauncherFirst():Boolean{
    return isLauncher() && isFirst()
  }

  fun isFirst(): Boolean{
    return serviceHistory.size == 0 && (query == null || (query.gesture == null && query.instruction == null))
  }

  fun getScreenGuardOrNot(): Boolean{
     return serviceHistory.size < 1
  }

  fun getInitialPromptOrInteract(): Boolean{
    if (serviceHistory.size== 1){
      if(query?.gesture == null && query?.instruction !=null )
        return false
    }
    return serviceHistory.size < 2
  }

  fun getHistory(state: NuixState): String {
    val conversationHistory = StringBuilder()

    for (query in state.queryHistory) {
      query.instruction?.let { instruction ->
        conversationHistory.append("\"user\": \"$instruction\"; ")
      }

      query.response.forEach { response ->
        conversationHistory.append("\"assistant\": the result of \"${response.data.serviceName}\" is \"${response.data.messageContent}\"; ")
      }
    }

    // Remove the last semicolon and space for a clean ending
    if (conversationHistory.isNotEmpty()) {
      conversationHistory.setLength(conversationHistory.length - 2)
    }

    return conversationHistory.toString()
  }


}
