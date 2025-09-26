package com.hcifuture.nuixserver.verticle

import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.nuix.NuixUserManager
import com.hcifuture.nuixserver.nuix.data.NuixContext
import com.hcifuture.nuixserver.nuix.data.NuixQuery
import com.hcifuture.nuixserver.session.ui.ElementTree
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.instance
import java.io.File

class NuixVerticle: CoroutineVerticle(), CoroutineEventBusSupport {
  private val nuixUserManager: NuixUserManager by Inject.di.instance()

  override suspend fun start() {
//    vertx.eventBus().consumer("nuix.session.context.post") { message ->
//      val json: JsonObject = message.body()
//      val userId: String = json.getString("userId")!!
//      val sessionId: String = json.getString("sessionId")!!
//      val uiTree: ElementTree? = json.getString("uiTree")?.let { ElementTree(File(it).readText()) }
//      val screenshot: File? = json.getString("screenshot")?.let { File(it) }
//      CoroutineScope(Dispatchers.IO).launch {
//        message.reply(
//          JsonObject()
//            .put("result",
//              nuixUserManager
//                .getUser(userId)
//                ?.getSession(sessionId)
//                ?.updateContext(NuixContext(
//                  uiTree = uiTree,
//                  screenshot = screenshot,
//                ))
//                ?.joinToString(separator = "\n")
//            )
//        )
//      }
//    }
//
//    vertx.eventBus().consumer("nuix.session.query.post") { message ->
//      val json: JsonObject = message.body()
//      val userId: String = json.getString("userId")!!
//      val sessionId: String = json.getString("sessionId")!!
//      val instruction: String? = json.getString("instruction")
//      val gesture: String? = json.getString("gesture")
//      val audio: File? = json.getString("audio")?.let { File(it) }
//      CoroutineScope(Dispatchers.IO).launch {
//        message.reply(
//          JsonObject()
//            .put("result",
//              nuixUserManager
//                .getUser(userId)
//                ?.getSession(sessionId)
//                ?.processQuery(NuixQuery(
//                  instruction = instruction,
//                  gesture = gesture,
//                  audio = audio,
//                ))
//                ?.joinToString(separator = "\n")
//            )
//        )
//      }
//    }
//
//    vertx.eventBus().consumer("nuix.session.query.stream.post") { message ->
//      val json: JsonObject = message.body()
//      val userId: String = json.getString("userId")!!
//      val sessionId: String = json.getString("sessionId")!!
//      val instruction: String? = json.getString("instruction")
//      val gesture: String? = json.getString("gesture")
//      val audio: File? = json.getString("audio")?.let { File(it) }
//      CoroutineScope(Dispatchers.IO).launch {
//        nuixUserManager
//          .getUser(userId)
//          ?.getSession(sessionId)
//          ?.processQueryFlow(NuixQuery(
//            instruction = instruction,
//            gesture = gesture,
//            audio = audio,
//          ))?.collect {
//            vertx.eventBus().send("nuix.session.query.stream.response",
//              JsonObject()
//                .put("sessionId", sessionId)
//                .put("data", it)
//            )
//          }
//        vertx.eventBus().send("nuix.session.query.stream.response",
//          JsonObject()
//            .put("sessionId", sessionId)
//            .put("data", "<END>")
//        )
//      }
//    }
  }
}
