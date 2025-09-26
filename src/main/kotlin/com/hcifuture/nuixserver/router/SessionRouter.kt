package com.hcifuture.nuixserver.router

import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.session.NuixSession
import com.hcifuture.nuixserver.session.NuixSessionManager
import com.hcifuture.nuixserver.util.getFile
import com.hcifuture.nuixserver.util.getParam
import com.hcifuture.nuixserver.util.requestEventBus
import com.hcifuture.nuixserver.util.respondJson
import io.ktor.client.request.*
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.instance

class SessionRouter {

  companion object {
    private val nuixSessionManager: NuixSessionManager by Inject.di.instance()

    fun create(vertx: Vertx, router: Router) {
      // Create a session
      router.post("/session").respond { ctx ->
        val userId = getParam(ctx, "userId")!!
        Future.succeededFuture(
          JsonObject().put("id", nuixSessionManager.openSession(userId).sessionId)
        )
      }

      // Delete a session
      router.delete("/session")
        .handler { ctx ->
          val sessionId = getParam(ctx, "sessionId")!!
          nuixSessionManager.closeSession(sessionId)
          respondJson(ctx, JsonObject().put("sessionId", sessionId))
        }

      // Update global contexts
      router.post("/context")
        .handler { ctx ->
          requestEventBus(vertx, ctx, "api.context.post",
            JsonObject()
              .put("userId", getParam(ctx, "userId")!!)
              .put("uiTree", getFile(ctx, "uiTree")?.uploadedFileName())
              .put("screenshot", getFile(ctx, "screenshot")?.uploadedFileName())
          )
        }

      // Update contexts related to the session
      router.post("/session/context")
        .handler { ctx ->
          requestEventBus(vertx, ctx, "api.session.context.post",
            JsonObject()
              .put("sessionId", getParam(ctx, "sessionId")!!)
              .put("uiTree", getFile(ctx, "uiTree")?.uploadedFileName())
              .put("screenshot", getFile(ctx, "screenshot")?.uploadedFileName())
          )
        }

      // Request in the session
      router.post("/session/instruction")
        .handler { ctx ->
          requestEventBus(vertx, ctx, "api.session.instruction.post",
            JsonObject()
              .put("sessionId", getParam(ctx, "sessionId")!!)
              .put("instruction", getParam(ctx, "instruction"))
              .put("gesture", getParam(ctx, "gesture"))
              .put("audio", getFile(ctx, "audio")?.uploadedFileName())
          )
        }

    }
  }
}
