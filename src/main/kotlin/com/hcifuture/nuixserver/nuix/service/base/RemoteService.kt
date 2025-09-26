package com.hcifuture.nuixserver.nuix.service.base

import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.data.*
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.SwaggerParseResult
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class RemoteService(
  val configFile: String
) : NuixService() {
  val swaggerParseResult: SwaggerParseResult = OpenAPIParser().readLocation(configFile, null, null)

  override val name: String = swaggerParseResult.openAPI.info.title
  override val description: String = swaggerParseResult.openAPI.info.description
  override var slots: List<Slot> = listOf()
  override var displayText: String = swaggerParseResult.openAPI.info.title

  override fun ruleBasedGuard(state: NuixState): Boolean {
    return state.currentContext.uiAnalysisData?.packageName == swaggerParseResult.openAPI.info.extensions["x-package-name"]
      && state.currentContext.uiAnalysisData?.page == swaggerParseResult.openAPI.info.extensions["x-page"]
  }

  override fun createDefaultResponse(
    displayText: String,
    index: Int?,
    isEnd: Boolean,
    defaultAction: String
  ): NuixResponse {
    return createTextResponse("", displayText, 0, true, messageState = MessageState.BRIEF)
  }

  override suspend fun updateContext(state: NuixState) {
    //TODO("Not yet implemented")
  }

  val webClientOptions = WebClientOptions().apply {
    connectTimeout = 60000
  }
  private val webClient: WebClient = WebClient.create(Vertx.vertx(), webClientOptions)

  suspend fun request(state: NuixState, request: JsonObject): Flow<NuixResponse> {
    println("!!!!!!!")
    println(request)
    val path = request.getString("path")!!
    val url = swaggerParseResult.openAPI.servers[0].url + path
    //val requestDetail = swaggerParseResult.openAPI.paths.get(request.getString("path"))
    val requestDetail = swaggerParseResult.openAPI.paths.get(path)
    val params = fillRequestBody(path, state, request.getJsonObject("requestBody"))
    if (url.isEmpty()) return flowOf()
    println("request url: $url, params: $params")
    return flow {
      val result = webClient.postAbs(url).sendJsonObject(params).coAwait()
      println("==========")
      println(result.body().toString())
      println("==========")
      println(result.bodyAsJsonArray())
      println("==========")
      val response = result.bodyAsJsonArray()
      for (i in 0..<response.size()) {
        val responseData =
          NuixResponse.json.decodeFromString<NuixResponseData>(response.getJsonObject(i).toString()).apply {
            messageId = serviceInstanceId
            itemId = "$serviceInstanceId.$i"
            displayText = swaggerParseResult.openAPI.info.title
            serviceName = swaggerParseResult.openAPI.info.title
          }
        emit(
          NuixResponse(
            data = responseData
          )
        )
      }
    }
  }

  fun fillRequestBody(path: String, state: NuixState, requestBody: JsonObject?): JsonObject {
    val params = JsonObject()
    val requestDetail = swaggerParseResult.openAPI.paths.get(path)
    requestDetail?.let {
      it.post.requestBody.content["application/json"]?.schema?.properties?.forEach { (t, u) ->
        when (t) {
          "nuix.uid" -> state.currentContext.uid
          "nuix.ui.data" -> state.currentContext.uiAnalysisData?.data
          "nuix.query.instruction" -> state.query?.instruction ?: ""
          "nuix.query.gesture" -> state.query?.gesture
          else -> requestBody?.getString(t, null)
        }?.let {
          params.put(t, it)
        }
      }
    }
    return params
  }
  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val defaultPath = swaggerParseResult.openAPI.info.extensions["x-default-path"] as String
    val url = swaggerParseResult.openAPI.servers[0].url + defaultPath
    val params = fillRequestBody(defaultPath, state, null)
    if (url.isEmpty()) return flowOf()
    println("request url: $url, params: $params")
    return flow {
      val result = webClient.postAbs(url).sendJsonObject(params).coAwait()
      println("==========")
      println(result.body().toString())
      println("==========")
      println(result.bodyAsJsonObject())
      println("==========")
      val responseData =
        NuixResponse.json.decodeFromString<NuixResponseData>(result.bodyAsJsonObject().toString()).apply {
          messageId = serviceInstanceId
          itemId = serviceInstanceId
          displayText = swaggerParseResult.openAPI.info.title
          serviceName = swaggerParseResult.openAPI.info.title
        }
      emit(
        NuixResponse(
          data = responseData
        )
      )
    }
  }
}
