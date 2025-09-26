package com.hcifuture.nuixserver.tool.crawler

import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h2
import it.skrape.selects.html5.li
import it.skrape.selects.html5.ol

class Crawler {
  fun searchBing(prompt: String): List<SearchItem> {
    val items: MutableList<SearchItem> = mutableListOf()
    skrape(HttpFetcher) {
      request {
        url = "https://www.bing.com/search?q=${prompt}"
        userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36"
        timeout = 3000
      }
      response {
        htmlDocument {
          ol {
            withId = "b_results"
            findFirst {
              try {
                li {
                  withClass = "b_algo"
                  findAll {
                    forEach { block ->
                      items.add(SearchItem(
                        url = block.div {
                          withClass = "tpmeta"
                          findFirst { text }
                        },
                        title = block.h2 {
                          findFirst { text }
                        },
                        content = block.div {
                          withClass = "b_caption"
                          findFirst { text }
                        }
                      ))
                    }
                  }
                } // li b_algo
              } catch (e: Exception) {
                println(html)
              }
            }
          } // ol b_results
        }
      } // response
    }
    return items
  }
}
