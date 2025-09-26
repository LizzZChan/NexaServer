package com.hcifuture.nuixserver.nuix

import com.hcifuture.nuixserver.nuix.data.NuixContext

class NuixMemory {
  private val contextHistory: MutableList<NuixContext> = mutableListOf()
}
