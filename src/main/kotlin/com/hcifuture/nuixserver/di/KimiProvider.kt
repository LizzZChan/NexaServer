package com.hcifuture.nuixserver.di

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.hcifuture.nuixserver.config.Config
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class KimiProvider(override val di: DI): DIAware {
  private val config: Config by instance()
  fun create(): OpenAI {
    return OpenAI(token = config.kimi.apiKey, host = OpenAIHost(config.kimi.server))
  }
}
