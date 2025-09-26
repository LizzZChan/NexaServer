package com.hcifuture.nuixserver.nuix

import java.util.*

class NuixUserManager {
  private val users: LinkedHashMap<String, NuixUser> = LinkedHashMap()

  fun getUser(id: String): NuixUser? = users[id]

  fun createUser(uid: String, name: String): NuixUser {
    // val id = UUID.randomUUID().toString()
    val user = NuixUser(name = name, id = uid)
    users[uid] = user
    return user
  }

  fun removeUser(id: String): NuixUser? {
    return users.remove(id)
  }
}
