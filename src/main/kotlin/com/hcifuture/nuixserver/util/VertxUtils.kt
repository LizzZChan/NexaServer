package com.hcifuture.nuixserver.util

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext

private fun respondJsonReply(ctx: RoutingContext, reply: AsyncResult<Message<JsonObject>>) {
  if (reply.succeeded()) {
    respondJson(ctx, reply.result().body())
  } else {
    ctx.fail(reply.cause())
  }
}

fun requestEventBus(
  vertx: Vertx,
  ctx: RoutingContext,
  address: String,
  message: JsonObject,
) {
  vertx.eventBus().request( address, message )
  { reply -> respondJsonReply(ctx, reply) }
}

fun respondJson(ctx: RoutingContext, json: JsonObject) {
  ctx.response()
    .putHeader("Content-Type", "application/json")
    .end(json.encodePrettily())
}

fun getParam(ctx: RoutingContext, param: String): String?
  = ctx.request().getParam(param)

fun getFile(ctx: RoutingContext, name: String): FileUpload?
  = ctx.fileUploads().find { it.name() == name }
