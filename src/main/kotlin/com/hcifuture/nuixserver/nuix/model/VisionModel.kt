package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.config.Config
import com.hcifuture.nuixserver.di.Inject
import com.hcifuture.nuixserver.nuix.data.NuixState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File
import java.util.Base64

class VisionModel(
  private val model: String = "gpt-4o", //vision-preview",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance("openai")

  fun encodeImage(imageFile: File): String {
    return Base64.getEncoder().encodeToString(imageFile.readBytes())
  }

  suspend fun performQA(imageFile: File, state: NuixState): ChatCompletion{
    val base64Image = encodeImage(imageFile)
    val reqList = ArrayList<ContentPart>()
    val textPart = """
      - 用户指令: ${state.query?.instruction}${'$'}
      - 界面上的文本信息：${state.currentContext.uiTree?.treeText}

    """.trimIndent()
    reqList.add(TextPart(textPart))
    reqList.add(ImagePart("data:image/jpeg;base64,$base64Image"))

    val qaPrompt = """
      我们实现了一个类似于三星circleToSearch的交互方式，根据用户在手机界面上圈选的内容来回复用户。
      给定用户圈选的裁剪片段（图片内容），该内容部分所在的layout中包括的文本，来回复用户指令。
      直接用一句话来回复，不要解释
    """.trimIndent()

    val messages: List<ChatMessage> = listOf(
      ChatMessage(
        role = ChatRole.System,
        content = qaPrompt
      ),
      ChatMessage(
        role = ChatRole.User,
        content = reqList
      )
    )

    return openAI.chatCompletion(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
      ),
    )

  }



  suspend fun performOcr(imageFile: File, circleTextFromUiTree:String): ChatCompletion{
    val base64Image = encodeImage(imageFile)

    val reqList = ArrayList<ContentPart>()
    reqList.add(TextPart("UiTree: $circleTextFromUiTree"))
    reqList.add(ImagePart("data:image/jpeg;base64,$base64Image"))

    val ocrPrompt = """
      我们实现了一个类似于三星circleToSearch的交互方式，根据用户在手机界面上圈选的内容来推荐服务。
      给定用户圈选的裁剪片段（图片内容），以及该内容部分所在的layout中包括的文本，判断用户想要圈选的内容。
      其中用户圈选的内容可以是图片，也可能是文字信息。其中，图片包含的内容可能会比用户真实想要圈选的内容要多(当圈选关键词时，由于胖手指问题，会将其他无关信息也圈进来)；也有可能会更少（当圈选大段内容时，用户可能只是在段落中心部分圈一下，会缺失周围的其他文本）。
      因此，需要综合图片信息和layout节点信息(uiTree)来共同判断用户的真实圈选目标。

      Step1：判断用户圈选的内容是图片还是文字。
      Step2：图片识别过程：
        如果用户圈选的是图片，则用一句话描述图片内容，例如短袖上衣，零食，或者风光图描述等。
        如果用户圈选的是文本，则对图片进行预处理，删除周围的不完整信息，然后进行ocr，提取图片中的文本信息(textFromImage)。根据textFromImage和uiTree来判断用户的圈选目标文本(circleText)
      Step3：输出格式：
        以json的格式输出，例如{"type": "IMAGE","content": "Image Content"} or {"type": "TEXT","content": "circle text"}


      例1:
      Input:
        - Image: 一张防晒霜的图
        - uiTree：null
      Output：
        {"type": "IMAGE","content": "图中有一只防晒霜放在桌子上"}

      例2:
      Input:
        - Image: 一张风景图
        - uiTree：null
      Output：
        {"type": "IMAGE","content": "图中有雪山，还有两个在骑马的女生。"}

      例3：
      Input:
        - textFromImage：荼香飘远
        - uiTree:茶香飘远 这是一个藏在深山里的人间秘境;
      Output:
        {"type": "TEXT","content": "茶香飘远"}


      例4：
      Input:
        - textFromImage：ar Thermal Pov; Northern Cape P
        - uiTree：Chinese tech to light up homes in South Africa, , An aerial view of the construction site of the Redstone Concentrated Solar Thermal Power Project near Postmasburg in Northern Cape Province of South Africa. [Photo provided to China Daily], An aerial view of the construction site of the Redstone Concentrated Solar Thermal Power Project near Postmasburg in Northern Cape Province of South Africa. [Photo provided to China Daily]];

      Output:
        {"type": "TEXT","content": "An aerial view of the construction site of the Redstone Concentrated Solar Thermal Power Project near Postmasburg in Northern Cape Province of South Africa."}



      以json的形式直接返回结果，不要解释。
    """.trimIndent()

    val messages: List<ChatMessage> = listOf(
      ChatMessage(
        role = ChatRole.System,
        content = ocrPrompt
      ),
      ChatMessage(
        role = ChatRole.User,
        content = reqList
      )
    )

    return openAI.chatCompletion(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
        responseFormat = ChatResponseFormat.JsonObject
      ),
    )

  }

  suspend fun query(messages: List<ChatMessage>): ChatCompletion {
    return openAI.chatCompletion(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
      )
    )
  }

  fun queryFlow(messages: List<ChatMessage>): Flow<ChatCompletionChunk> {
    return openAI.chatCompletions(
      request = ChatCompletionRequest(
        model = ModelId(model),
        messages = messages,
      ),
    )
  }
}
