package com.hcifuture.nuixserver.session.ui

import com.google.gson.Gson
import com.hcifuture.nuixserver.auth.JWTAuthProc
import kotlin.math.max
import kotlin.math.min
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class ElementNodeBounds(
  val top: Int,
  val bottom: Int,
  val left: Int,
  val right: Int,
) {
  companion object {
    fun fromPoints(points: List<PathPoint>): ElementNodeBounds {
      val xs = points.map { it.x }
      val ys = points.map { it.y }
      return ElementNodeBounds(
        top = ys.min().toInt(),
        bottom = ys.max().toInt(),
        left = xs.min().toInt(),
        right = xs.max().toInt(),
      )
    }
  }

  fun inside(bounds: ElementNodeBounds): Boolean {
    return top >= bounds.top && bottom <= bounds.bottom &&
           left >= bounds.left && right <= bounds.right
  }

  fun contain(bounds: ElementNodeBounds): Boolean {
    return bounds.inside(this)
  }

  fun intersect(bounds: ElementNodeBounds): Boolean {
    return max(top, bounds.top) <= min(bottom, bounds.bottom) &&
           max(left, bounds.left) <= min(right, bounds.right)
  }
}

data class ElementNode(
  val accessibilityFocused: Boolean?,
  val bounds: ElementNodeBounds?,
  val checkable: Boolean?,
  val checked: Boolean?,
  val children: List<ElementNode>?,
  val className: String?,
  val clickable: Boolean?,
  val descendantCount: Int?,
  val dismissable: Boolean?,
  val editable: Boolean?,
  val enabled: Boolean?,
  val focusable: Boolean?,
  val focused: Boolean?,
  val invisibleToUser: Boolean?,
  val level: Int?,
  val longClickable: Boolean?,
  val packageName: String?,
  val password: Boolean?,
  val scrollable: Boolean?,
  val selected: Boolean?,
  val text: String?,
) {
  fun traverse(): List<ElementNode> {
    val nodes = mutableListOf<ElementNode>()
    nodes.add(this)
    children?.forEach { nodes.addAll(it.traverse()) }
    return nodes
  }
}

class ElementTree(
  private val treeStr: String,
) {
  private var root: ElementNode = Gson().fromJson(treeStr, ElementNode::class.java)
  private val treeJsonObject: JSONObject = JSONObject(treeStr)
  val treeText = mutableListOf<String>().apply {
    extractTextFromJson(treeJsonObject, this)
  }


  init {
    extractTextFromJson(treeJsonObject, treeText)
  }

  fun extractText(bounds: ElementNodeBounds): List<String> {
    return root.traverse()
      .filter {
        it.bounds != null && it.text != null && it.bounds.intersect(bounds)
      }
      .map { it.text!! }
  }

  fun getPackageName(): String? {
    try {
      val jsonObject = JSONObject(treeStr)
      // 递归搜索所有的className字段，直到找到包含packageName的节点
      fun searchPackageName(node: JSONObject): String? {
        if (node.has("packageName")) {
          return node.getString("packageName")
        }
        val children = node.optJSONArray("children")
        children?.forEach {
          if (it is JSONObject) {
            val packageName = searchPackageName(it)
            if (packageName != null) {
              return packageName
            }
          }
        }
        return null
      }
      return searchPackageName(jsonObject)
    } catch (e: Exception) {
      // 如果解析过程中发生错误，返回null
      return null
    }
  }


  private fun extractTextFromJson(json: JSONObject, texts: MutableList<String>) {
    if (json.has("text") && json.getString("text").isNotBlank() && !json.getBoolean("invisibleToUser")) {
      texts.add(json.getString("text"))
    }
    if (json.has("contentDescription") && json.getString("contentDescription").isNotBlank() && !json.getBoolean("invisibleToUser")) {
      texts.add(json.getString("contentDescription"))
    }
    if (json.has("children")) {
      val children = json.getJSONArray("children")
      for (i in 0 until children.length()) {
        extractTextFromJson(children.getJSONObject(i), texts)
      }
    }
  }
}
