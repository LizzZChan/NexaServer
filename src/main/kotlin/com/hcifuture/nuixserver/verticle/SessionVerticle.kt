package com.hcifuture.nuixserver.verticle

import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.session.NuixSessionManager
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.instance

class SessionVerticle: CoroutineVerticle(), CoroutineEventBusSupport {

  private val nuixSessionManager: NuixSessionManager by Inject.di.instance()

  override suspend fun start() {
    vertx.eventBus().consumer("api.session.context.post") { message ->
      val json: JsonObject = message.body()
      val sessionId: String? = json.getString("sessionId")
      val uiTree: String? = json.getString("uiTree")
      val screenshot: String? = json.getString("screenshot")
      CoroutineScope(Dispatchers.IO).launch {
        val updateResult = nuixSessionManager
          .getSession(sessionId)
          ?.updateContext(
            uiTree = uiTree,
            screenshot = screenshot,
          )
        val replyJson = JsonObject()
          .put("result", updateResult)
        message.reply(replyJson)
      }
    }

    vertx.eventBus().consumer("api.session.instruction.post") { message ->
      val json: JsonObject = message.body()
      val sessionId: String? = json.getString("sessionId")
      val gesture: String? = json.getString("gesture")
      val instruction: String? = json.getString("instruction")
      val audio: String? = json.getString("audio")
      CoroutineScope(Dispatchers.IO).launch {
        val queryResult = nuixSessionManager
          .getSession(sessionId)
          ?.queryFromWeb(
            instruction = instruction,
            gestureStr = gesture,
            audio = audio,
          )
        val replyJson = JsonObject()
          .put("result", queryResult)
        message.reply(replyJson)
      }
    }

  }

  override suspend fun stop() {

  }
}
