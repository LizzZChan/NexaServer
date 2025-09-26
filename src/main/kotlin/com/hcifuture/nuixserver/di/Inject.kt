package com.hcifuture.nuixserver.di

import com.aallam.openai.client.OpenAI
import com.hcifuture.nuixserver.config.Config
import com.hcifuture.nuixserver.nuix.NuixUserManager
import com.hcifuture.nuixserver.session.NuixSessionManager
import com.hcifuture.nuixserver.tool.crawler.Crawler
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceOrFileSource
import com.sksamuel.hoplite.addResourceSource
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.nio.file.Path

class Inject {
  companion object {
    lateinit var di: DI
    fun init() {
      di = DI {
        bindSingleton<Config> {
          ConfigLoaderBuilder.default()
            .addFileSource(Path.of("config.yml").toFile(), optional = true)
            .addResourceSource("/config.yml", optional = true)
            .build()
            .loadConfigOrThrow<Config>()
        }
        bindSingleton<OpenAI>(tag = "openai") {
          OpenAIProvider(di).create()
        }
        bindSingleton<OpenAI>(tag = "kimi") {
          KimiProvider(di).create()
        }
        bindSingleton<NuixSessionManager> {
          NuixSessionManager()
        }
        bindSingleton<NuixUserManager> {
          NuixUserManager()
        }
        bindSingleton<Crawler> {
          Crawler()
        }
      }
    }
  }
}
