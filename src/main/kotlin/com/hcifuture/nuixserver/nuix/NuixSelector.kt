package com.hcifuture.nuixserver.nuix

import com.hcifuture.nuixserver.nuix.data.NuixState

abstract class NuixSelector {
  abstract suspend fun select(allServices: MutableList<NuixService>, state: NuixState): MutableList<NuixService>
}
