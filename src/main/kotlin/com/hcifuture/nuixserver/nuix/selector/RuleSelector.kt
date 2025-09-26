package com.hcifuture.nuixserver.nuix.selector

import com.hcifuture.nuixserver.nuix.NuixSelector
import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.createServiceInstance
import com.hcifuture.nuixserver.nuix.data.ConversationState
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.model.slotFilling
import com.hcifuture.nuixserver.nuix.service.*
import com.hcifuture.nuixserver.nuix.service.base.RemoteService
import io.vertx.core.json.JsonObject
import kotlinx.serialization.json.Json
import kotlin.reflect.full.primaryConstructor

class RuleSelector : NuixSelector() {
  override suspend fun select(allServices: MutableList<NuixService>, state: NuixState): MutableList<NuixService> {
    val ruleServices: MutableList<NuixService> = mutableListOf()

    val  selectedServices = allServices.filter {
      it.ruleBasedGuard(state)
    }.toMutableList()
    selectedServices.removeAll { service -> state.serviceHistory.flatten().any { it.name == service.name } }
    for (service in selectedServices) {
      var serviceInstance: NuixService? = null
      if (service.name == "WeatherService" && state.isLauncherFirst()) {
        serviceInstance = WeatherService()
        serviceInstance.setDefaultSlots()
      } else {
        serviceInstance = createServiceInstance(service.name)
        val missingSlots = service.getMissingSlots()
        if (serviceInstance != null && missingSlots.isNotEmpty()) {
          val llmSlots = slotFilling(service, state)
          println("service:${serviceInstance.name}, llmSlots: $llmSlots")
          if (llmSlots != "null") {
            serviceInstance.updateSlots(Json.decodeFromString<kotlinx.serialization.json.JsonObject>(llmSlots))
          }
        }
      }

      serviceInstance?.let {
        ruleServices.add(it)
      }
    }

  println("ruleSelector: size ${ruleServices.size}")
  ruleServices.forEach { service ->
    println("Service Name: ${service.name}")
    println("Service Slots: ${service.slots}")
  }
  return ruleServices
  }
}
