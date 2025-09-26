package com.hcifuture.nuixserver.nuix.selector

import com.aallam.openai.api.audio.speechRequest
import com.hcifuture.nuixserver.auth.JWTAuthProc
import com.hcifuture.nuixserver.nuix.*
import com.hcifuture.nuixserver.nuix.data.*
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.service.*
import com.hcifuture.nuixserver.nuix.service.base.TextService
import io.ktor.http.*
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.primaryConstructor
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDate

class LLMSelector : NuixSelector() {
  val chatSession: ChatSession = ChatSession(ChatModel("gpt-4o"))
  val logger: Logger = LoggerFactory.getLogger(JWTAuthProc::class.java)

  val json = Json {
    coerceInputValues = true

  }
  val initialPrompt: NuixPrompt = NuixPrompt(
    """
        你是一个手机上的服务推荐助手，根据用户在界面上圈的内容（circle）和自然语言指令（instruction）为用户推荐服务（service）。
        ###用户输入：
          ###
          - 用户在界面上选中的信息：#{circleText}
          - 用户的自然语言指令：#{instruction}
          - 界面信息：#{screen}
          - 界面上的文字信息包括：#{treeText}
          ###
          此外，现在的时间是 #{currentTime}。

        ###推理过程：
          Step 1: 根据用户的圈选内容和指令推理用户意图，根据用户意图从服务列表中选择一个服务；
          以下是服务列表及描述, 格式为：serviceName: "serviceDescription":
          #{servicesDescription}

          Step 2: 对于选中的服务，从用户的圈选内容和语音指令(if not null)以及界面信息抽取相关信息来补充服务中的槽值，如果没有相关的槽值，则为null。其中，slot value要根据slotType要求的格式输出。
          以下是每个服务对应的slots以及slotType format，格式为：{"serviceName":"service.name", "serviceSlots": {"slotName":"slotValue" (slotType),...,}}
          #{slotsDescription}


        ### Output Example：
          [
            {
              "serviceName": "searchService",
              "serviceSlots": {"content": "三北"}
            },
            {
              "serviceName": "WeatherService",
              "serviceSlots": {"position": "北京", "time": "2024-06-19"}
            }
          ]

        以json的形式直接返回预测的服务，不要解释，至少从服务列表中选择一个服务。请优先选择其他服务，而不是聊天服务。
    """
  )
  val interactPrompt: NuixPrompt = NuixPrompt(
    """
      对于之前用户圈选的内容: #{old_circleText}, 自然语言指令: #{old_instruction}, 界面内容: #{screen}, 系统的预测结果为: #{predictedService}.
      针对该预测结果，用户圈选了内容 #{circleText}, 输入了指令:#{instruction}, 根据用户的反馈更新服务以及对应的槽值。

      用户输入：
      ###
        - 用户在界面上选中的信息：#{circleText}
        - 用户的自然语言指令：#{instruction}
        - 界面信息：#{screen}
        - 界面内包含的文本内容:"#{treeText}"
        ###
        此外，现在的时间是 #{currentTime}，地点是 #{currentLocation}。

      ### 推理过程：
        Step 1: 请根据用户的来判断用户的交互意图，包括：
        {
          1: "之前预测的服务或者槽值错误，用户进行修改。";
          2: "之前预测的服务缺少槽值信息，用户进行补充。";
          3: "用户开启了一个新的话题，与之前的话题无关。";
        }
        Step 2: 根据用户的交互意图推理用户需要的服务；
        以下是服务列表及描述, 格式为：serviceName: "serviceDescription":
        #{servicesDescription}

        Step 3: 对于选中的服务，从用户的圈选内容和语音指令(if not null)以及界面信息抽取相关信息来补充服务中的槽值，如果没有相关的槽值，则为null。其中，slot value要根据slotType要求的格式输出。
        以下是每个服务对应的slots以及slotType format，格式为：{"serviceName":"service.name", "serviceSlots": {"slotName":"slotValue" (slotType),...,}}
        #{slotsDescription}

      ### 以json的形式直接返回预测的服务，不要解释, 至少从服务列表中选择一个服务
        """
  )



  private suspend fun selectQuery(
    services: List<NuixService>,
    state: NuixState,
    isFirst: Boolean = false,
  ): String {
    val prompt = if (isFirst) { initialPrompt } else { interactPrompt }
      .reset()
      .apply {
        fill("screen", state.currentContext.screenDescription)
        val circleTextValue = state.query?.circleText?.takeIf { it.isNotEmpty() }?.let { text ->
          when (state.query.circleType) {
            CircleType.TEXT -> "- 用户在界面上圈选的是【文本信息】，内容是${text}"
            CircleType.IMAGE -> "- 用户在界面上圈选的是【图片信息】，内容是${text}"
            else -> "- 用户在界面上选中的信息：${text}"
          }
        } ?: ""
        fill("circleText", circleTextValue)
        fill("instruction", state.query?.instruction)
        fill("servicesDescription", services.joinToString("; ") {
          "${it.name}:\"${it.description}\""
        })
        fill("treeText", state.currentContext.uiTree?.treeText?.joinToString(separator = " "))
        val jsonObjects = services.map { service ->
          buildJsonObject {
            put("serviceName", service.name)
            put("serviceSlots", buildJsonObject {
              service.slots.forEach { slot ->
                put(slot.name, JsonPrimitive("${slot.description} (${slot.type.description})"))
              }
            })
          }
        }

        val jsonArray = JsonArray(jsonObjects)
        val slotsDescription = json.encodeToString(JsonArray.serializer(), jsonArray)

        fill("slotsDescription", slotsDescription)
        fill("old_instruction", state.getSecondLastQuery()?.instruction)
        fill("old_circleText", state.getSecondLastQuery()?.circleText)
        fill("predictedService", state.serviceHistory.lastOrNull()?.joinToString(",") {
          val slotsFormatted = it.slots.joinToString(", ") { slot -> "${slot.name}:${slot.value}" }
          "[${it.name}: {$slotsFormatted}] "
        })
        fill("currentTime", LocalDate.now().toString())
        fill("currentLocation", state.currentContext.currentLocation)
      }
    println("llm selector prompt:${prompt.prompt}")
    return chatSession.query(prompt = prompt, retJson = true).content?.trim() ?: "null"
  }



  override suspend fun select(allServices:MutableList<NuixService>, state: NuixState): MutableList<NuixService> {
    val llmServices = mutableListOf<NuixService>()

    var llmResponse = selectQuery(allServices, state, state.getInitialPromptOrInteract())
    println("llmResponse:$llmResponse")
    if (llmResponse.isNotEmpty() && llmResponse[0] == '{') {
      llmResponse = "[$llmResponse]"
    }
    val serviceDefinitions = Json.decodeFromString<JsonArray>(llmResponse)

    for (i in 0..<serviceDefinitions.size) {
      val definition = serviceDefinitions[i].jsonObject
      val serviceName = definition["serviceName"]?.jsonPrimitive?.content
      if(serviceName != null && state.services.none { it.name == serviceName } ) {
        createServiceInstance(serviceName)?.let {
          definition.getOrDefault("serviceSlots", null)?.let { slot ->
            it.updateSlots(slot.jsonObject)
          }
          llmServices.add(it)
        }
      }

    }


    println("llmSelector:")
    llmServices.forEach { service ->
      println("Service Name: ${service.name}")
      println("Service Slots: ${service.slots}")
    }
    state.services.addAll(llmServices)

    return llmServices

  }


}


