package com.insieme.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ScrapedInfo(val title: String?, val imageUrl: String?)

object LinkScraper {
    suspend fun scrape(url: String): ScrapedInfo = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .header("Accept-Language", "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(10000)
                .get()
            
            val title = doc.select("meta[property=og:title]").attr("content")
                .ifBlank { doc.title() }
                .ifBlank { doc.select("h1").first()?.text() }

            val imageUrl = doc.select("meta[property=og:image]").attr("content")
                .ifBlank { doc.select("img#landingImage").attr("src") } // Specifico per Amazon
                .ifBlank { doc.select("img[data-old-hires]").attr("src") }
                .ifBlank { doc.select("link[rel=image_src]").attr("href") }

            ScrapedInfo(title, imageUrl)
        } catch (e: Exception) {
            ScrapedInfo(null, null)
        }
    }
}
