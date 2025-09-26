package com.hcifuture.nuixserver.auth

import com.google.gson.Gson
import com.hcifuture.nuixserver.config.Config
import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.nuix.model.ResponseMessage
import com.hcifuture.nuixserver.util.HttpUtils
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.impl.jose.JWT
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import org.htmlunit.org.apache.http.HttpStatus
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern

class JWTAuthProc(val vertx: Vertx): Handler<RoutingContext> {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(JWTAuthProc::class.java)

    fun extractUserInfoFromToken(authorization: String): JsonObject? {
      val head = "Bearer "
      try {
        if (authorization.startsWith(head)) {
          val token = authorization.substring(head.length)
          val splitArray = token.split(".")
          if (splitArray.size < 2) {
            return null
          }
          val decodeStr = String(Base64.getUrlDecoder().decode(splitArray[1]))
          val uidPattern: Pattern = Pattern.compile("\"uid\"\\s*:\\s*\"([a-z0-9-]+)\"")
          val matcher = uidPattern.matcher(decodeStr)
          if (matcher.find()) {
            val uid = matcher.group(1)
            return JsonObject().put("uid", uid)
          } else {
            return null
          }
        } else {
          return null
        }
      } catch (e: Exception) {
        logger.error("extractUserInfoFromToken fail, authorization: ${authorization}", e)
        return null
      }
    }
  }

  private val config: Config by Inject.di.instance()

  private val gson by lazy {
    Gson()
  }

  private val webClient: WebClient by lazy {
    WebClient.create(vertx)
  }

  private val jwt: JWT by lazy {
    JWT()
  }

  private fun checkToken(authorization: String): Future<HttpResponse<Buffer>> {
    return webClient.get(config.bas.port, config.bas.host, "/token-check/").putHeader("Authorization", authorization)
      .send()
  }

//  private fun getAccountInfo(authorization: String): Future<HttpResponse<JsonObject>> {
//     return webClient.get(config.bas.port, config.bas.host, "/tplus/account")
//       .putHeader("Authorization", authorization)
//       .send()
//       .andThen {
//
//          return@andThen JsonObject()
//       }
//  }

  override fun handle(context: RoutingContext) {
    HttpUtils.dumpRequest(context, logger)
    HttpUtils.setHttpHeader(context.response())
    HttpUtils.exceptionHandler(context.vertx(), context.response())
    val authorization = context.request().getHeader("Authorization")
    if (authorization.isNullOrEmpty()) {
      logger.info("Authorization is null")
      context.setUser(User.create(JsonObject().put("uid", "Test")))
      context.next()
//      context.response()
//        .setStatusCode(HttpStatus.SC_UNAUTHORIZED)
//        .end(gson.toJson(ResponseMessage(HttpStatus.SC_UNAUTHORIZED, "no authorization")))
      return
    }
    checkToken(authorization).onSuccess {
      if (it.statusCode() in 200..299) {
        val userJsonObject = extractUserInfoFromToken(authorization)
        val user = User.create(userJsonObject)
        context.setUser(user)
        context.next()
      } else {
        logger.info("auth fail remote status code ${it.statusCode()}")
        context.response().setStatusCode(it.statusCode()).end(it.body())
      }
    }.onFailure {
      logger.error("auth fail: ${it.message}", it)
      context.response()
        .setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        .end(gson.toJson(ResponseMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, "check token error")))
    }
  }
}
