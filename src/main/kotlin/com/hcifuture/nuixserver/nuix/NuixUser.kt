package com.hcifuture.nuixserver.nuix

import java.util.*

class NuixUser(val name: String, val id: String) {
  private val sessions: LinkedHashMap<String, NuixSession> = LinkedHashMap()
  private val memory: NuixMemory = NuixMemory()

  fun getSession(id: String): NuixSession? = sessions[id]

  fun createSession(name: String): NuixSession {
    val id = UUID.randomUUID().toString()
    val session = NuixSession(
      id = id,
      name = name,
      memory = memory,
    )
    sessions[id] = session
    return session
  }

  fun removeSession(sessionId: String): NuixSession? {
    return sessions.remove(sessionId)
  }
}
