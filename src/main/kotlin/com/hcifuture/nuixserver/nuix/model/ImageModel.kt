package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageEdit
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.di.Inject
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.awt.Image

class ImageModel(
  private val model: String = "dall-e-3",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance("openai")

  suspend fun create(
    prompt: String,
    size: ImageSize = ImageSize.is1024x1024,
  ): ImageURL {
    return create(prompt, 1, size)[0]
  }

  suspend fun create(
    prompt: String,
    count: Int,
    size: ImageSize = ImageSize.is1024x1024,
  ): List<ImageURL> {
    return openAI.imageURL(
      creation = ImageCreation(
        prompt = prompt,
        model = ModelId(model),
        n = count,
        size = size,
      )
    )
  }

  suspend fun edit(
    image: String,
    imageSource: okio.Source,
    mask: String,
    maskSource: okio.Source,
    prompt: String,
    size: ImageSize = ImageSize.is256x256,
  ): ImageURL {
    return edit(image, imageSource, mask, maskSource, prompt, 1, size)[0]
  }

  suspend fun edit(
    image: String,
    imageSource: okio.Source,
    mask: String,
    maskSource: okio.Source,
    prompt: String,
    count: Int,
    size: ImageSize = ImageSize.is256x256,
  ): List<ImageURL> {
    return openAI.imageURL(
      edit = ImageEdit(
        image = FileSource(name = image, source = imageSource),
        model = ModelId(model),
        mask = FileSource(name = mask, source = maskSource),
        prompt = prompt,
        n = count,
        size = size,
      )
    )
  }
}
