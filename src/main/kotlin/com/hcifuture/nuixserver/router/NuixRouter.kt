package com.hcifuture.nuixserver.router

import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.nuix.NuixUserManager
import com.hcifuture.nuixserver.nuix.data.InputMode
import com.hcifuture.nuixserver.nuix.data.NuixContext
import com.hcifuture.nuixserver.nuix.data.NuixQuery
import com.hcifuture.nuixserver.nuix.data.UIAnalysisData
import com.hcifuture.nuixserver.session.ui.ElementTree
import com.hcifuture.nuixserver.util.getFile
import com.hcifuture.nuixserver.util.getParam
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


private val json = Json {
  ignoreUnknownKeys = true
}

class NuixRouter {

  companion object {
    private val nuixUserManager: NuixUserManager by Inject.di.instance()

    private val logger: Logger = LoggerFactory.getLogger(NuixRouter::class.java)

    fun writeSSEData(ctx: RoutingContext, data: String) {
        ctx.response().write("data: $data \n\n")
    }

    fun create(vertx: Vertx, router: Router) {
      router.post("/nuix/user/session").respond { ctx ->
        val authUser = ctx.user()
        val uid = authUser.get<String>("uid")
        val sessionName = getParam(ctx, "sessionName")!!
        var user = nuixUserManager.getUser(uid)
        if (user == null) {
           user = nuixUserManager.createUser(uid, "")
        }
        Future.succeededFuture(
          JsonObject().put("sessionId", user.createSession(sessionName).id)
        )
      }

      router.delete("/nuix/user/session").respond { ctx ->
        val authUser = ctx.user()
        val uid = authUser.get<String>("uid")
        val sessionId = getParam(ctx, "sessionId")!!
        Future.succeededFuture(
          JsonObject().put("sessionId", nuixUserManager.getUser(uid)?.removeSession(sessionId)?.id)
        )
      }

      router.post("/nuix/user/session/context")
        .handler { ctx ->
          val authUser = ctx.user()
          val uid = authUser.get<String>("uid")
          val sessionId = getParam(ctx, "sessionId")!!
          val uiTree = getFile(ctx, "uiTree")?.uploadedFileName()?.let {
            ElementTree(File(it).readText())
          }
          val uiAnalysisData = ctx.request().getFormAttribute("uiAnalyzeData")?.let {
            println("UIANALYSISDATA:$$it$")
            json.decodeFromString<UIAnalysisData>(it)
          }

          val screenshot = getFile(ctx, "screenshot")?.uploadedFileName()?.let { File(it) }
          CoroutineScope(Dispatchers.Default).launch {
            val result = nuixUserManager
                .getUser(uid)
                ?.getSession(sessionId)
                ?.updateContext(
                  NuixContext(
                    uid = uid,
                    uiTree = uiTree,
                    screenshot = screenshot,
                    uiAnalysisData = uiAnalysisData
                  )
                )
                ?.joinToString(separator = "\n")
            ctx.response()
              .putHeader("Content-Type", "application/json")
              .end(JsonObject().put("result", result).encodePrettily())
          }
        }

      router.post("/nuix/user/session/request")
        .handler { ctx ->
          //val requestBody = JsonObject(ctx.request().params().toString())
          // Get all query parameters
          val messageId = getParam(ctx, "messageId")!!
          val authUser = ctx.user()
          val uid = authUser.get<String>("uid")
          val sessionId = getParam(ctx, "sessionId")!!
          val response = ctx.response().apply {
            setChunked(true)
            headers().add("Content-Type", "text/event-stream;charset=UTF-8")
            headers().add("Connection", "keep-alive")
            headers().add("Cache-Control", "no-cache")
            headers().add("Access-Control-Allow-Origin", "*")
          }
          val jsonParams = JsonObject().apply {
            put("requestBody", JsonObject(getParam(ctx, "requestBody")))
            put("uid", uid)
            put("sessionId", sessionId)
            put("messageId", messageId)
            put("path", getParam(ctx, "path"))
          }
          val job = CoroutineScope(Dispatchers.Default).launch {
            val result = nuixUserManager
              .getUser(uid)
              ?.getSession(sessionId)?.apply {
                requestRemoteServiceFlow(messageId, jsonParams).collect {
                  writeSSEData(ctx, it.toString())
                }
              }
            response.end("[DONE]")
          }
          response.closeHandler {
            job.cancel()
          }

        }
      router.post("/nuix/user/session/query")
        .handler { ctx ->
          val authUser = ctx.user()
          val uid = authUser.get<String>("uid")
          val sessionId = getParam(ctx, "sessionId")!!
          val instruction = getParam(ctx, "instruction")
          val gesture = getParam(ctx, "gesture")
          val audio = getFile(ctx, "audio")?.uploadedFileName()?.let { File(it) }
          val stream = getParam(ctx, "stream").toBoolean()
          val inputMode = getParam(ctx, "inputMode")
          val lat = getParam(ctx, "lat")?.toDouble()
          val lng = getParam(ctx, "lng")?.toDouble()
          logger.info("current uid: $uid , sessionId: $sessionId, instruction: $instruction, stream:$stream")
          if (!stream) {
            CoroutineScope(Dispatchers.Default).launch {
              val result = nuixUserManager
                .getUser(uid)
                ?.getSession(sessionId)
                ?.processQuery(
                  NuixQuery(
                    instruction = instruction,
                    inputMode = InputMode.fromString(inputMode),
                    gesture = gesture,
                    audio = audio,
                    lat = lat,
                    lng = lng
                  )
                )
                ?.joinToString(separator = "\n") { it.data.messageContent }
              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(JsonObject().put("result", result).encodePrettily())
            }
          } else {
            val response = ctx.response()
            response.setChunked(true)
            response.headers().add("Content-Type", "text/event-stream;charset=UTF-8")
            response.headers().add("Connection", "keep-alive")
            response.headers().add("Cache-Control", "no-cache")
            response.headers().add("Access-Control-Allow-Origin", "*")
            val job = CoroutineScope(Dispatchers.Default).launch {
              val stringBuilder = StringBuilder()
              nuixUserManager
                .getUser(uid)
                ?.getSession(sessionId)
                ?.apply {
//                  getParam(ctx, "messageId")?.let {
//                    this.executeServiceFlow(it)
//                  }
                  logger.info("begin processQueryFlow: $instruction")
                  processQueryFlow(
                    NuixQuery(
                      instruction = instruction,
                      inputMode = InputMode.fromString(inputMode),
                      gesture = gesture,
                      audio = audio,
                    )
                  ).collect {
//                    msg.put("text", it.messageContent)
//                    writeSSEData(ctx, msg)
//                    logger.info("sse data: $it.toJsonObject().toString()")
                    writeSSEData(ctx, it.toString())
                    stringBuilder.append(it.data.messageContent)
                  }
                }
              response.end("[DONE]")
              logger.info("query complete: \n $stringBuilder")
            }
            response.closeHandler {
              job.cancel()
            }
          }
        }

      router.post("/nuix/user/session/requestMessage")
        .handler { ctx ->
          val authUser = ctx.user()
          val uid = authUser.get<String>("uid")
          val sessionId = getParam(ctx, "sessionId")!!
          val messageId = getParam(ctx, "messageId")!!
          val stream = getParam(ctx, "stream").toBoolean()
          logger.info("current uid: $uid , sessionId: $sessionId, messageId: $messageId, stream:$stream")
          if (!stream) {
            CoroutineScope(Dispatchers.Default).launch {
              val result = messageId?.let {
                nuixUserManager
                  .getUser(uid)
                  ?.getSession(sessionId)
                  ?.executeServiceFlow(it)
              }

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(JsonObject().put("result", result).encodePrettily())
            }
          } else {
            val response = ctx.response()
            response.setChunked(true)
            response.headers().add("Content-Type", "text/event-stream;charset=UTF-8")
            response.headers().add("Connection", "keep-alive")
            response.headers().add("Cache-Control", "no-cache")
            response.headers().add("Access-Control-Allow-Origin", "*")
            val job = CoroutineScope(Dispatchers.Default).launch {
              val stringBuilder = StringBuilder()
              nuixUserManager
                .getUser(uid)
                ?.getSession(sessionId)
                ?.apply {
                logger.info("requestMessage，messageId: $messageId")
                  executeServiceFlow(messageId).collect {
            //                    msg.put("text", it.messageContent)
            //                    writeSSEData(ctx, msg)
            //                    logger.info("sse data: $it.toJsonObject().toString()")
                    writeSSEData(ctx, it.toString())
                    stringBuilder.append(it.data.messageContent)
                  }
                }

              response.end("[DONE]")
              logger.info("request complete: \n $stringBuilder")
            }
            response.closeHandler {
              job.cancel()
            }
          }
        }
    }
  }
}


