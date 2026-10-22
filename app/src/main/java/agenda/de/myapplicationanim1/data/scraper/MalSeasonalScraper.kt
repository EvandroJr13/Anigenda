package agenda.de.myapplicationanim1.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Item de anime sazonal extraído diretamente do site oficial do MyAnimeList para o ano atual (2026).
 */
data class SeasonalAnimeItem(
    val id: String,
    val name: String,
    val coverUrl: String,
    val tags: List<String>,
    val studio: String,
    val releaseDate: String, // Data convertida de mm/dd/yyyy para dd/mm/yyyy
    val pvUrl: String? = null
)

class MalSeasonalScraper {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }

    /**
     * Faz o Web Scraping em tempo real da página de temporada atual do MyAnimeList (ano 2026).
     */
    suspend fun scrapeSeasonalAnimes(): List<SeasonalAnimeItem> = withContext(Dispatchers.IO) {
        try {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR) // 2026
            val month = cal.get(Calendar.MONTH) + 1
            val season = when (month) {
                1, 2, 3 -> "winter"
                4, 5, 6 -> "spring"
                7, 8, 9 -> "summer"
                10, 11, 12 -> "fall"
                else -> "winter"
            }
            val dynamicUrl = "https://myanimelist.net/anime/season/$year/$season"

            val doc = Jsoup.connect(dynamicUrl)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(30000)
                .get()

            val list = mutableListOf<SeasonalAnimeItem>()
            val cards = doc.select("div.seasonal-anime")

            for (card in cards) {
                try {
                    // Nome do anime
                    val titleLink = card.select("a.link-title").first() ?: card.select("h2.h2_anime_title a").first()
                    val name = titleLink?.text()?.trim() ?: continue
                    val malUrl = titleLink.attr("href")
                    val id = extractMalId(malUrl)

                    // Capa do anime
                    val imgElem = card.select("img.lazyload, img").first()
                    val coverUrl = imgElem?.attr("data-src")?.takeIf { it.isNotBlank() } ?: imgElem?.attr("src") ?: ""

                    // Tags / Gêneros
                    val tags = card.select("span.genre a, div.genres span a").map { it.text().trim() }

                    // Studio
                    val studio = card.select("span.studio a, div.source ~ span a").first()?.text()?.trim() ?: "Studio Desconhecido"

                    // Data de Lançamento / PV / Aired (mm/dd/yyyy convertido para dd/mm/yyyy)
                    val airedElem = card.select("div.info span.item, div.broadcast, div.property").first()
                    val rawDateText = airedElem?.text()?.trim() ?: ""
                    val formattedDate = parseAndConvertDate(rawDateText, year)

                    // Link do PV / Trailer
                    val pvLink = card.select("a.js-video-link, a.iframe, a.video-promo").first()?.attr("href")

                    list.add(
                        SeasonalAnimeItem(
                            id = id,
                            name = name,
                            coverUrl = coverUrl,
                            tags = tags,
                            studio = studio,
                            releaseDate = formattedDate,
                            pvUrl = pvLink
                        )
                    )
                } catch (_: Exception) {}
            }

            list.distinctBy { it.id }
        } catch (e: Exception) {
            throw ScrapingException("Falha ao raspar a página season do MyAnimeList: ${e.message}", e)
        }
    }

    private fun extractMalId(url: String): String {
        val regex = Regex("/anime/(\\d+)")
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: url.hashCode().toString()
    }

    /**
     * Converte datas no formato mm/dd/yyyy (ou texto equivalente) para dd/mm/yyyy no ano atual (2026).
     */
    private fun parseAndConvertDate(dateStr: String, currentYear: Int): String {
        if (dateStr.isBlank()) {
            return "01/01/$currentYear"
        }
        return try {
            val pattern = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{4})")
            val match = pattern.find(dateStr)
            if (match != null) {
                val mm = match.groupValues[1].padStart(2, '0')
                val dd = match.groupValues[2].padStart(2, '0')
                val yyyy = match.groupValues[3].toInt()
                // Garante que o ano seja o atual (2026) se vier desatualizado
                val finalYear = if (yyyy < 2026) currentYear else yyyy
                return "$dd/$mm/$finalYear"
            }
            
            val today = LocalDate.now()
            today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            "01/01/$currentYear"
        }
    }
}
