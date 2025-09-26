package com.hcifuture.nuixserver.nuix.model

import com.hcifuture.nuixserver.nuix.ChatSession
import com.hcifuture.nuixserver.nuix.NuixPrompt
import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.data.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

suspend fun slotFilling(service:NuixService, state:NuixState):String{
  val chatSession = ChatSession(ChatModel("gpt-3.5-turbo"))
  val prompt = NuixPrompt("""
    对于service: #{serviceName}, 从下面信息中抽取其slots的value: [#{slots}]。如果没有相关的信息，则返回null。

    Step1：抽取槽值内容。如果存在用户在界面上选中的信息和自然语言指令, 则从中抽取槽值；否则，则根据屏幕文本信息判断。
    ###
    #{instruction}
    #{circleText}
    #{screen}
    ###
    此外，现在的时间是 #{currentTime}，地点是 #{currentLocation}。

    Step2：根据slotType格式要求输出内容，其中#{slotFormat}。

    以json的形式直接返回结果，格式为：{"slotName":"slotValue","slotName":"slotValue",...,}, 不要解释。

  """)
    .apply {
      fill("serviceName", service.name)
      fill("slots", service.slots.joinToString(", ") {
        it.name
      })
      fill("currentTime", LocalDate.now().toString())
      fill("currentLocation", state.currentContext.currentLocation)
      // 根据条件赋值 screen
      val screenValue = if (state.currentContext.uiTree?.getPackageName() in launchers) {
        "目前是在手机主屏幕"
      } else {
        state.currentContext.uiTree?.treeText?.joinToString(separator = " ") ?: ""
      }
      fill("screen", "- 界面信息：$screenValue")

      val circleTextValue = state.query?.circleText?.takeIf { it.isNotEmpty() }?.let { text ->
        when (state.query.circleType) {
          CircleType.TEXT -> "- 用户在界面上圈选的是【文本信息】，内容是${text}"
          CircleType.IMAGE -> "- 用户在界面上圈选的是【图片信息】，内容是${text}"
          else -> "- 用户在界面上选中的信息：${text}"
        }
      } ?: ""
      fill("circleText", circleTextValue)

      // 如果 instruction 不为 null 或者为空，则赋值
      val instructionValue = state.query?.instruction?.takeIf { it.isNotEmpty() }?.let {
        "- 用户的自然语言指令：${it}"
      } ?: ""
      fill("instruction", instructionValue)
      fill("slotFormat", service.slots.joinToString(", ") {
        "${it.name}: ${it.type.description}"
      })
    }

  println("slotFilling prompt: ${prompt.prompt}")

  return chatSession.query(prompt).content?:""

}

suspend fun generateQuestions(service: NuixService, state: NuixState): String {
  val chatSession = ChatSession(ChatModel("gpt-3.5-turbo"))
  val prompt = NuixPrompt("""
    在预测过程中，对于用户数据:
    ###
    #{circleText}
    自然语言指令: #{instruction}
    界面内容: #{screen}
    ###

    系统推荐的服务为: [#{serviceName}(#{serviceDescription})#{assignedSlots}].
    但是还缺少部分slots: [#{missingSlots}].

    请生成一句话总结服务推荐以及slots赋值的理由，并告诉用户缺少的missing slots，向用户获取信息。

    例子1：根据指令“翻译”，我猜测您需要翻译服务，但是我不清楚你想要翻译什么内容。
    例子2：根据当前界面是主界面，我猜测您需要查询天气，但我不知道您的时间和地点。

    直接返回结果，不要超过30个字, 语气生动一些。
  """)
    .apply {
      fill("screen", state.currentContext.screenDescription)
      fill("instruction", state.query?.instruction)
      fill("serviceName", service.name)
      fill("serviceDescription", service.description)
      val circleTextValue = state.query?.circleText?.takeIf { it.isNotEmpty() }?.let { text ->
        when (state.query.circleType) {
          CircleType.TEXT -> "- 用户在界面上圈选的是【文本信息】，内容是${text}"
          CircleType.IMAGE -> "- 用户在界面上圈选的是【图片信息】，内容是${text}"
          else -> "- 用户在界面上选中的信息：${text}"
        }
      } ?: ""
      fill("circleText", circleTextValue)
      val missingSlots = service.getMissingSlots()
      val missingInfo = missingSlots.joinToString(separator = "，") { slot ->
        "${slot.name}: ${slot.value}"
      }
      val assignedSlots = service.slots.filter { slot -> !missingSlots.contains(slot) }
      val assignedInfo = ": " + assignedSlots.joinToString(separator = "，") { slot ->
        "${slot.name}: ${slot.value}"
      }
      fill("missingSlots", missingInfo)
      fill("assignedSlots", assignedInfo)
    }

  println("generateQuestions prompt: ${prompt.prompt}")
  println("generateQuestions response: ${chatSession.query(prompt).content?:""}")
  return chatSession.query(prompt).content?:""

}
