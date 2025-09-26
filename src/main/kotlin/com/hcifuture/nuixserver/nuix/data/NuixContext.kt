package com.hcifuture.nuixserver.nuix.data

import com.hcifuture.nuixserver.session.ui.ElementTree
import java.io.File

data class NuixContext (
  val uid: String? = null,
  val uiTree: ElementTree? = null,
  val screenshot: File? = null,
  val screenDescription: String? = null,
  var circledImage: File? = null,
  var uiAnalysisData: UIAnalysisData? = null,
  var currentLocation: String? = null,
) {
  companion object {
    fun empty(): NuixContext {
      return NuixContext(null, null)
    }
  }
  fun update(new: NuixContext): NuixContext {
    return NuixContext(
      uiTree = new.uiTree ?: uiTree,
      screenshot = new.screenshot ?: screenshot,
      screenDescription = new.screenDescription ?: screenDescription,
      circledImage = new.circledImage ?: circledImage,
      uiAnalysisData = new.uiAnalysisData ?: uiAnalysisData,
      uid = new.uid ?: uid,
    )
  }
}
