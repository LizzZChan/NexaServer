package com.hcifuture.nuixserver.nuix

class NuixPrompt(val template: String) {
  var prompt: String = template.trimIndent()

  fun reset(): NuixPrompt {
    prompt = template.trimIndent()
    return this
  }

  fun fill(name: String, value: String?) : NuixPrompt {
    prompt = prompt.replace("#{${name}}", value ?: "null")
    return this
  }

  // TODO: load from files
}
