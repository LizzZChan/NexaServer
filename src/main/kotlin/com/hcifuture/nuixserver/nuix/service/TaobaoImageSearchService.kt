package com.hcifuture.nuixserver.nuix.service

import com.hcifuture.nuixserver.nuix.NuixService
import com.hcifuture.nuixserver.nuix.data.*
import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.multipart.MultipartForm
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.util.Base64

class TaobaoImageSearchService() : NuixService() {
  override val name: String = "TaobaoImageSearchService"
  override val description: String = "淘宝搜索，针对图片类搜索"
  override var slots: List<Slot> = listOf()
  override var displayText = "拍立淘"

  private val option = WebClientOptions().apply {
    connectTimeout = 10000
  }
  val client: WebClient = WebClient.create(Vertx.vertx(), option)

  override fun ruleBasedGuard(state: NuixState): Boolean {
    return false
//    return state.query!!.circleType == CircleType.IMAGE
  }

  override suspend fun updateContext(state: NuixState) {
    println("")
  }

  private fun file2Base64(file: File): String {
    val bytes = file.readBytes()
    val base64 = Base64.getEncoder().encodeToString(bytes)
    val mimeType = when (file.extension.lowercase()) {
      "jpg", "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      else -> throw IllegalArgumentException("Unsupported file type")
    }
    return "data:$mimeType;base64,$base64"
  }


  override suspend fun processQueryFlow(state: NuixState): Flow<NuixResponse> {
    val baseURL = "http://api.veapi.cn/tbk/picsimilaritem?vekey=V85545900F40072432"
    val screen = state.currentContext.circledImage ?: return flowOf(createTextResponse("imagesearch null"))
    val form = MultipartForm.create()
      .attribute("pic", file2Base64(screen))

    return flow {
      val httpResponse = client.postAbs(baseURL)
        .sendMultipartForm(form)
        .coAwait()
      if (httpResponse.statusCode() != 200) {
        emit(createTextResponse("image search:network fail"))
      } else {
        val resp = httpResponse.bodyAsJsonObject()
        println("result: $resp")
        if (resp.getString("error", "") != "0") {
          emit(createTextResponse("image search:service fail"))
        } else {
          val data = resp.getJsonArray("data")
          val len = data.size()
          for (i in 0..<len) {
            //emit(createTextResponse(data.getJsonObject(i).toString()))
            val itemData = data.getJsonObject(i).getJsonObject("Result")
            val priceStr = "￥${itemData.getString("PriceAfterCoupon", "??")}>"

            val res = NuixResponse(
              data =
              NuixResponseDataIconItem().apply {
                text = priceStr +
                  itemData.getString("ShortTitle", "").ifEmpty {
                    itemData.getString("Title")
                  }
                messageId = serviceInstanceId
                imageUrl = itemData.getString("PicUrl")
                url = "https://detail.tmall.com/item.htm?id=${itemData.getString("ItemId")}"
                serviceName = name
                serviceDisplayText = "拍立淘"
                itemId = "$serviceInstanceId.$i"
              })
            println("EMIT:$res")
            emit(res)
          }
        }
      }
    }
  }
}
