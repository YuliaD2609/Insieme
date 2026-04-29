package com.insieme.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object LinkScraper {
    data class Metadata(val title: String?, val imageUrl: String?)

    suspend fun getMetadata(url: String): Metadata = withContext(Dispatchers.IO) {
        try {
            val response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("Accept-Language", "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(10000)
                .followRedirects(true)
                .get()
            
            // TITLE EXTRACTION
            var title = response.select("#productTitle").text().trim()
            if (title.isBlank()) title = response.select("meta[property=og:title]").attr("content").trim()
            if (title.isBlank()) title = response.select("meta[name=title]").attr("content").trim()
            if (title.isBlank()) title = response.title().trim()

            // IMAGE EXTRACTION
            var imageUrl = ""
            
            // 1. Try Amazon's dynamic image JSON
            val dynamicImageElement = response.select("#landingImage, #imgBlkFront, #main-image").firstOrNull()
            val dynamicImageData = dynamicImageElement?.attr("data-a-dynamic-image")
            
            if (!dynamicImageData.isNullOrBlank()) {
                // The format is {"URL":[width,height], ...}
                // We take the first URL found in the JSON string
                val regex = """"(https://.*?)" """.toRegex()
                val match = regex.find(dynamicImageData)
                if (match != null) {
                    imageUrl = match.groupValues[1]
                }
            }

            // 2. Fallback to standard src
            if (imageUrl.isBlank()) {
                imageUrl = dynamicImageElement?.attr("src") ?: ""
            }

            // 3. Fallback to OpenGraph
            if (imageUrl.isBlank() || !imageUrl.startsWith("http")) {
                imageUrl = response.select("meta[property=og:image]").attr("content").trim()
            }

            // 4. Fallback to first large image
            if (imageUrl.isBlank() || !imageUrl.startsWith("http")) {
                imageUrl = response.select("img[src*=media], img[src*=amazon]").firstOrNull()?.attr("src") ?: ""
            }

            Metadata(
                title = if (title.length > 100) title.take(100) + "..." else title,
                imageUrl = imageUrl.takeIf { it.isNotBlank() && it.startsWith("http") }
            )
        } catch (e: Exception) {
            Metadata(null, null)
        }
    }
}
