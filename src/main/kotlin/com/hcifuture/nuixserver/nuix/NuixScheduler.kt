package com.hcifuture.nuixserver.nuix

import com.hcifuture.nuixserver.auth.JWTAuthProc
import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.model.generateQuestions
import com.hcifuture.nuixserver.nuix.selector.LLMSelector
import com.hcifuture.nuixserver.nuix.selector.RuleSelector
import com.hcifuture.nuixserver.nuix.service.*
import com.hcifuture.nuixserver.nuix.service.base.RemoteService
import com.hcifuture.nuixserver.session.ChatSession
import com.hcifuture.nuixserver.session.NuixSession
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class NuixScheduler {
//  private val allTextServices: MutableList<NuixService> = listOf(
//    AppSearchService(), WeatherService(), CalendarService(), ScanService(), SummarizeService(),
//    ChatService(), TranslateService(), ReplyService(), ClockService(), VisionQAService(), SearchService(),
//    RemoteService("./ArticleSummaryService.yml")
//  ).toMutableList()
//  private val allImageServices: MutableList<NuixService> = listOf(
//    TaobaoImageSearchService()
//  ).toMutableList()
  companion object {
    val allLauncherServices: MutableList<NuixService> = mutableListOf<NuixService>(
      WeatherService(), ScanService(),
      ChatService(), ClockService(), SearchService(),
      RemoteService("./ArticleSummaryService.yml")
    )

    val allTextServices: MutableList<NuixService> = mutableListOf<NuixService>(
      AppSearchService(), WeatherService(), CalendarService(), ScanService(), SummarizeService(),
      ChatService(), TranslateService(), ReplyService(), ClockService(), VisionQAService(), SearchService(),
      RemoteService("./ArticleSummaryService.yml")
    )

    val allImageServices: MutableList<NuixService> = mutableListOf(
      TaobaoImageSearchService()
    )

    val remoteServices = (allTextServices + allImageServices).filter {
      it is RemoteService
    }.associate {
      it.name to (it as RemoteService).configFile
    }
  }

  private val selectors: List<NuixSelector> = listOf(RuleSelector(), LLMSelector())

  val logger: Logger = LoggerFactory.getLogger(NuixScheduler::class.java)

  suspend fun updateContext(
    state: NuixState,
  ) {
    allTextServices.forEach {
      it.updateContext(state = state)
    }
    allImageServices.forEach {
      it.updateContext(state = state)
    }
  }
  private var firstServices = listOf<NuixService>()

  public suspend fun selectServices(state: NuixState): List<NuixService> {
    val allServices = serviceGuard(state)
    val selectedServices = mutableListOf<NuixService>()
    var remainingServices = allServices.toMutableList()

    selectors.forEach { selector ->
      val selected = selector.select(remainingServices, state)
      selectedServices.addAll(selected)
      remainingServices.removeAll(selected)
    }
    if (selectedServices.isEmpty()) {
      selectedServices.add(ChatService())
    }
    println("Selected services: $selectedServices")

    state.services = selectedServices
    state.serviceHistory.add(selectedServices)

    return selectedServices

  }

  fun serviceGuard(state: NuixState): MutableList<NuixService> {
    val allServices: MutableList<NuixService> = allTextServices.toMutableList()

    state.query?.gesture?.let {
      allServices.addAll(allImageServices)
    }

    if (state.isLauncherFirst()) {
      return allLauncherServices.toMutableList()
    }

//    if (!state.isFirst() && firstServices.isNotEmpty()) {
//      allServices.removeAll(firstServices)
//    }

    return allServices
  }


  suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val flow = channelFlow {
      selectAndProcessServices(state, this)
    }.onEach {
      state.query?.response?.add(it)
    }

    return flow
  }

  private suspend fun selectAndProcessServices(state: NuixState, channel: SendChannel<NuixResponse>) {
    logger.info("state:${state}, packageName: ${state.currentContext.uiTree?.getPackageName()}")

    val allServices = serviceGuard(state)
    val selectedServices = mutableListOf<NuixService>()
    val remainingServices = allServices.toMutableList()
    state.services = mutableListOf<NuixService>()
    state.services.clear()

    selectors.forEach { selector ->
      println("remaining services: $remainingServices")
      val selected = selector.select(remainingServices, state)
      if (selected.isNotEmpty()) {
        selectedServices.addAll(selected)
        state.services.addAll(selected)
        println("Selector: $selector, select: $selected,  Selected services: $selectedServices")
        for (service in selectedServices) {
          executeService(service, state, channel)
        }
        selected.forEach { selectedService ->
          remainingServices.removeIf { it.name == selectedService.name }
        }
        println("remaining services: $remainingServices")
      }
    }
    if (state.services.isEmpty()) {
      val chatService: NuixService = ChatService()
      state.services.add(chatService)
      executeService(chatService, state, channel)
    }
    println("Selected services: ${state.services}")


//    state.services = selectedServices
    state.serviceHistory.add(state.services)

  }

  suspend fun executeService(service: NuixService, state: NuixState, channel: SendChannel<NuixResponse>) {
    if (state.isFirst()) {
      channel.send(service.createDefaultResponse())
    } else {
      val missingSlots = service.getMissingSlots()
      if (missingSlots.isNullOrEmpty()) {
        println("Execute. service[${service.name}] with slots: ${service.slots}")
        service.processQueryFlow(state).collect { response ->
          channel.send(response)
        }
      } else {
        println("Query User (exist missing slots). service[${service.name}] with slots: ${service.slots}")
        val response = service.createTextResponse(generateQuestions(service, state), displayText = service.displayText)
        channel.send(response)
      }
    }
  }


  suspend fun executeService(serviceId: String, state: NuixState): Flow<NuixResponse> {
    println("executeService:" + serviceId + "services：" + state.services)
    var service = state.services.find { it.serviceInstanceId == serviceId }
    println("service:" + service!!.serviceInstanceId)
    // todo 执行服务后需要把firstService List 里面的维护一下，同时包括guard和ruleselector模块
    if (service!!.getMissingSlots().isNullOrEmpty()) {
      return service.processQueryFlow(state)
    } else {
      return flowOf(service.createTextResponse(generateQuestions(service, state), displayText = service.displayText))
    }
  }

  suspend fun requestRemoteService(serviceId: String, state: NuixState, request: JsonObject): Flow<NuixResponse> {
    val service = state.services.find { it.serviceInstanceId == serviceId }
    return service?.let {
      (it as RemoteService).request(state, request)
    }?: flowOf()
  }

  suspend fun processQuery(
    state: NuixState
  ): List<NuixResponse> {
    val response = selectServices(state)
      .map { it.processQuery(state) }
      .flatten()
    return response
  }
}
