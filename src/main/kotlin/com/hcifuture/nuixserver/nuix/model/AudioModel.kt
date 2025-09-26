package com.hcifuture.nuixserver.nuix.model

import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.Transcription
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.audio.Voice
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.di.Inject
import org.kodein.di.DIAware
import org.kodein.di.instance

class AudioModel(
  private val ttsModel: String = "tts-1",
  private val asrModel: String = "whisper-1",
): DIAware {
  override val di by lazy { Inject.di }
  private val openAI: OpenAI by instance("openai")

  suspend fun tts(
    input: String,
    voice: Voice = Voice.Alloy,
  ): ByteArray {
    return openAI.speech(
      request = SpeechRequest(
        model = ModelId(ttsModel),
        input = input,
        voice = voice,
      )
    )
  }

  suspend fun asr(
    name: String,
    source: okio.Source,
  ): Transcription {
    return openAI.transcription(
      request = TranscriptionRequest(
        audio = FileSource(name = name, source = source),
        model = ModelId(asrModel),
      )
    )
  }
}
