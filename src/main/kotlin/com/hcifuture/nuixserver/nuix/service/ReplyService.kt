package com.hcifuture.nuixserver.nuix.service

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Role
import com.hcifuture.nuixserver.nuix.data.NuixResponse
import com.hcifuture.nuixserver.nuix.data.NuixState
import com.hcifuture.nuixserver.nuix.data.Slot
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.service.base.TextService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.function.BinaryOperator
import java.util.stream.Collectors

class ReplyService() : TextService() {
  override val name = "ReplyService"
  override val description = "在聊天界面上根据历史聊天内容自动生成回复内容。"
  override var slots: List<Slot> = listOf(
    // Slot("content", "String", "I am in a meeting right now. I will get back to you later.",true, "自动回复的内容"),
    //Slot("person", "String", "Bob",true, "回复的对象")
  )
  override var displayText: String = "消息自动回复"
  override fun ruleBasedGuardInstruction(state: NuixState): Boolean {
    val keywords = listOf("帮我回", "回复", "微信回", "替我回", "发消息")
    return state.currentContext.uiAnalysisData != null &&
    keywords.any { keyword -> state.query?.instruction?.contains(keyword) ?: false }
  }


//  override suspend fun processInstruction(instruction: String, state: NuixState): List<NuixResponse> {
//    return listOf(createTextResponse(data = "service:replyMessage"))
//  }

  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val messageChunk = state.currentContext.uiAnalysisData?.let { uiAnalysisData ->
      if (uiAnalysisData.page != "") {
        uiAnalysisData.messages.stream().map {
          (if (it.isSelf) "我" else it.source) + "：" + it.content
        }.collect(Collectors.joining("\n"))
      } else "${uiAnalysisData.contactName}：你好"
    }
    val systemPrompt = """
      你是一个聪明友善幽默的聊天高手，请根据聊天记录用自然语言回复他人。
      要求：
      - 充当“我”的角色，给出5条回复
      - 回复必须简洁，多用短句，20个字以内
      - 回复末尾少用句号
      - 使用自然语言回答，口语化，以一个真实人类的口吻说话
      - 多共情，少给建议，少加油，不问“有什么可以帮你的”

    """.trimIndent()
    val messages = mutableListOf(
      ChatMessage(role = Role.System, content = systemPrompt),
      ChatMessage(
        role = Role.User, content = """我：好累啊
冷冰凝：坚持一下，马上就到周末了
我："""
      ),
      ChatMessage(
        role = Role.Assistant, content = """- 终于可以放松一下
- 你有什么安排吗
- 我有点想去打羽毛球，你要去吗
- 还有3天呢
- 是哦！要不要周末组织一点活动"""
      ),
      ChatMessage(
        role = Role.User, content = """我：你们周末都干嘛了
安静：我吃了一家好吃的粤菜
夏明：是不是我上次带你去吃的那家
我：哇
我："""
      ),
      ChatMessage(
        Role.Assistant, content = """- 你们偷偷去吃了啥好吃的不带我
- 我也想吃
- 是学校旁边那家吗？
- 我最爱吃粤菜了，特别是煲仔饭
- 哪家啊 我怎么不知道"""
      ),
      ChatMessage(Role.User, content = "$messageChunk\n我：")
    )
    var inResult = false
    val totContent = StringBuffer()
    var currentContent = ""
    var resultNo = -1
    val startRegex = Regex("""- """)
    val endRegex = Regex("""\n""")
    println("MESSAGECHUNK:$messageChunk")
    return ChatModel().queryFlow(messages).map {
      it.choices[0].delta?.content ?: ""
    }.transform {
      totContent.append(it)
//      println("TOTCONTENT:$totContent")
      currentContent += it
      if (!inResult) {
        startRegex.find(currentContent)?.let { result ->
          currentContent = currentContent.removeRange(result.range)
          inResult = true
          resultNo++
        }
      }
      if (inResult) {
        endRegex.find(currentContent)?.let { result ->
          val res = currentContent.substring(0, result.range.first)
          val left = currentContent.substring(result.range.last + 1)
          emit(createTextResponse(res, index = resultNo, isEnd = true, defaultAction = "copy,fill_input"))
          currentContent = left
          inResult = false
        } ?: run {
          emit(createTextResponse(currentContent, index = resultNo, defaultAction = "copy,fill_input"))
          currentContent = ""
        }
      }
    }
  }

  //return flowOf(createTextResponse(data = "service:replyMessage, $slots"))

//    val prompt = """
//      根据界面信息以及用户输入帮用户自动回复消息。
//
//      ###
//      - 界面信息：${currentContext.uiTree?.treeText}\n
//      - 用户在界面上选中的信息：${query.circleText}\n
//      - 用户的自然语言指令: ${query.instruction}\n
//      ###
//
//      直接生成一句回复消息，不要超过15个字。
//
//    """.trimIndent()
//    return queryFlow(
//      model = model,
//      prompt = prompt
//    )
//  }
  /*
  resultNum = 5
[[chains]]
type="gptRequest"
temperature=0.6
model="gpt-3.5-turbo-0125"
[[chains.messages]]
role = "system"
content= '''
你是一个聪明友善幽默的聊天高手，请根据聊天记录用自然语言回复他人。
要求：
- 充当“我”的角色，给出5条回复
- 回复必须简洁，多用短句，20个字以内
- 回复末尾少用句号
- 使用自然语言回答，口语化，以一个真实人类的口吻说话
- 多共情，少给建议，少加油，不问“有什么可以帮你的”
'''
[[chains.messages]]
role = "user"
content = '''
我：好累啊
冷冰凝：坚持一下，马上就到周末了
我：
'''
[[chains.messages]]
role = "assistant"
content =  '''
- 终于可以放松一下
- 你有什么安排吗
- 我有点想去打羽毛球，你要去吗
- 还有3天呢
- 是哦！要不要周末组织一点活动
'''
[[chains.messages]]
role = "user"
content = '''
我：你们周末都干嘛了
安静：我吃了一家好吃的粤菜
夏明：是不是我上次带你去吃的那家
我：哇
我：
'''
[[chains.messages]]
role = "assistant"
content = '''
- 你们偷偷去吃了啥好吃的不带我
- 我也想吃
- 是学校旁边那家吗？
- 我最爱吃粤菜了，特别是煲仔饭
- 哪家啊 我怎么不知道
'''
[[chains.messages]]
role = "user"
content='''
<#include "messageChunk.ftl">
'''
[[chains]]
type="regexExtractor"
startRegex='''- '''
endRegex='''\n'''
   */
}
