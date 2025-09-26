package com.hcifuture.nuixserver.util

import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.htmlunit.org.apache.http.HttpStatus
import org.slf4j.Logger

class HttpUtils {
  companion object {
    fun exceptionHandler(vertx: Vertx, response: HttpServerResponse) {
      vertx.exceptionHandler { handler: Throwable ->
        response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        val rt = JsonObject()
        rt.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR)
        rt.put("msg", if (handler.cause != null) handler.cause!!.message else handler.toString())
        response.end(rt.encodePrettily())
      }
    }

    fun setHttpHeader(response: HttpServerResponse) {
      response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
    }

    fun dumpRequest(context: RoutingContext, logger: Logger) {
      val request = context.request()
      try {
        logger.info(
          "${request.method().name()} ${request.path()}${
            if (!request.query().isNullOrEmpty()) "?${request.query()}" else ""
          }"
        )
      } catch (e: Exception) {
        logger.error("dump request fail", e)
      }
    }
  }
}
