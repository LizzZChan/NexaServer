package com.hcifuture.nuixserver.di

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.hcifuture.nuixserver.config.Config
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class OpenAIProvider(override val di: DI): DIAware {
  private val config: Config by instance()
  fun create(): OpenAI {
    var host = OpenAIHost.OpenAI
    if (config.openai.baseUrl != null) {
      host = OpenAIHost(config.openai.baseUrl!!)
    }
    if (config.openai.proxy == null) {
      return OpenAI(token = config.openai.apiKey, host = host)
    } else {
      return OpenAI(token = config.openai.apiKey, host = host, proxy = ProxyConfig.Http(config.openai.proxy!!))
    }
  }
}
