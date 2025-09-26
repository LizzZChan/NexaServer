package com.hcifuture.nuixserver.tool.crawler

class SearchItem (
  val url: String,
  val title: String,
  val content: String,
) {

}

class CrawlerSearchResult(
  val items: List<SearchItem>,
) {
}
