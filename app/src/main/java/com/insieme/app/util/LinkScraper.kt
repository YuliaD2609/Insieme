package com.insieme.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ScrapedInfo(val title: String?, val imageUrl: String?)

object LinkScraper {
    suspend fun scrape(url: String): ScrapedInfo = withContext(Dispatchers.IO) {
        try {
            // Pulizia URL e gestione redirect
            val connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("Accept-Language", "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Cache-Control", "max-age=0")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Connection", "keep-alive")
                .followRedirects(true)
                .timeout(15000)

            val doc = connection.get()
            
            // Logica Titolo
            val title = doc.select("meta[property=og:title]").attr("content")
                .ifBlank { doc.select("#productTitle").text() } // Amazon specifico
                .ifBlank { doc.title() }
                .ifBlank { doc.select("h1").first()?.text() }

            // Logica Immagine (Amazon usa diversi ID a seconda della pagina)
            var imageUrl = doc.select("meta[property=og:image]").attr("content")
            
            if (imageUrl.isBlank()) {
                imageUrl = doc.select("img#landingImage").attr("src")
            }
            if (imageUrl.isBlank()) {
                imageUrl = doc.select("img#imgBlkFront").attr("src") // Libri
            }
            if (imageUrl.isBlank()) {
                imageUrl = doc.select("img#main-image").attr("src")
            }
            if (imageUrl.isBlank()) {
                val dataAHiRes = doc.select("img#landingImage").attr("data-a-dynamic-image")
                if (dataAHiRes.isNotBlank()) {
                    // Prendi il primo link nel JSON delle immagini dinamiche
                    val match = Regex("""https://[^"]+""").find(dataAHiRes)
                    imageUrl = match?.value ?: ""
                }
            }
            
            ScrapedInfo(title.trim(), imageUrl.trim())
        } catch (e: Exception) {
            e.printStackTrace()
            ScrapedInfo(null, null)
        }
    }
}
