package com.hcifuture.nuixserver

import com.aallam.openai.client.ProxyConfig
import com.hcifuture.nuixserver.auth.JWTAuthProc
import com.hcifuture.nuixserver.config.Config
import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.router.NuixRouter
import com.hcifuture.nuixserver.nuix.model.AudioModel
import com.hcifuture.nuixserver.nuix.model.ChatModel
import com.hcifuture.nuixserver.nuix.model.EmbeddingModel
import com.hcifuture.nuixserver.nuix.model.ImageModel
import com.hcifuture.nuixserver.nuix.service.base.RemoteService
import com.hcifuture.nuixserver.router.SessionRouter
import com.hcifuture.nuixserver.session.ChatSession
import com.hcifuture.nuixserver.session.NuixSession
import com.hcifuture.nuixserver.session.NuixSessionManager
import com.hcifuture.nuixserver.tool.crawler.Crawler
import com.hcifuture.nuixserver.verticle.NuixVerticle
import com.hcifuture.nuixserver.verticle.SessionVerticle
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MainVerticle : AbstractVerticle() {

  private val port: Int = 26665
  val croppedDir = "cropped"

  private val logger: Logger by lazy {
    LoggerFactory.getLogger(MainVerticle::class.java)
  }

  init {
    Inject.init()
  }

  private lateinit var jwtProc: JWTAuthProc

  override fun start(startPromise: Promise<Void>) {
    File(croppedDir).mkdirs()
    val router = Router.router(vertx)
    jwtProc = JWTAuthProc(vertx)
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploaded"))
    router.route().handler(jwtProc)
    NuixRouter.create(vertx, router)
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          logger.info("HTTP server started on port $port")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
