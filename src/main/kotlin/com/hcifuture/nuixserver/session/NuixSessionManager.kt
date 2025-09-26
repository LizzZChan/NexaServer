package com.hcifuture.nuixserver.session

import java.util.*

class NuixSessionManager {
  private val sessions: LinkedHashMap<String, NuixSession> = LinkedHashMap()

  fun getSession(sessionId: String?): NuixSession? {
    // TODO: don't return the last session.
    if (sessionId == null) {
      return sessions.entries.lastOrNull()?.value
    }
    return sessions[sessionId]
  }

  fun openSession(userId: String): NuixSession {
    val sessionId = UUID.randomUUID().toString()
    val session = NuixSession(userId, sessionId)
    sessions[sessionId] = session
    return session
  }

  fun closeSession(sessionId: String) {
    sessions[sessionId]?.close()
    sessions.remove(sessionId)
  }
}
