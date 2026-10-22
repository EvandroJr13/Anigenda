package agenda.de.myapplicationanim1.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * Representação dos dias da semana para o agendamento do MyAnimeList.
 */
enum class DayOfWeek(val queryKey: String, val displayNamePt: String) {
    MONDAY("monday", "Segunda"),
    TUESDAY("tuesday", "Terça"),
    WEDNESDAY("wednesday", "Quarta"),
    THURSDAY("thursday", "Quinta"),
    FRIDAY("friday", "Sexta"),
    SATURDAY("saturday", "Sábado"),
    SUNDAY("sunday", "Domingo");

    companion object {
        fun fromPt(pt: String): DayOfWeek {
            return entries.firstOrNull { it.displayNamePt.equals(pt, ignoreCase = true) } ?: MONDAY
        }
    }
}

/**
 * Plataforma de streaming com link direto ou de busca gerado dinamicamente.
 */
data class StreamingPlatform(
    val name: String,
    val url: String
)

/**
 * Item de anime extraído via Web Scraping do MyAnimeList.
 */
data class AnimeItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val malUrl: String,
    val streamingLinks: List<StreamingPlatform>,
    val airingDay: String,
    val score: Double = 0.0
)

/**
 * Exceção customizada para erros ocorridos durante o processo de scraping.
 */
class ScrapingException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Módulo de Web Scraping estruturado para extrair o calendário de animes do MyAnimeList
 * e realizar o cruzamento automático com plataformas de streaming.
 */
class MalWebScraper {

    companion object {
        private const val SCHEDULE_URL = "https://myanimelist.net/anime/season/schedule"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }

    /**
     * Obtém a lista de animes agendados para um determinado dia da semana através de Web Scraping do HTML do MAL.
     */
    suspend fun getScheduleByDay(dayOfWeek: DayOfWeek): List<AnimeItem> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(SCHEDULE_URL)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(30000)
                .get()

            val animeList = mutableListOf<AnimeItem>()

            // O site do MyAnimeList agrupa os animes por blocos de dias da semana (ex: id="monday", class="anime-header")
            val selectedHeader = doc.select("div.anime-header#${dayOfWeek.queryKey}, a[name=${dayOfWeek.queryKey}], h2:has(span:containsIgnoreCase(${dayOfWeek.displayNamePt}))").first()

            val elementsToParse: List<org.jsoup.nodes.Element> = if (selectedHeader != null) {
                val siblings = mutableListOf<org.jsoup.nodes.Element>()
                var next = selectedHeader.nextElementSibling()
                while (next != null && !next.hasClass("anime-header") && next.tagName() != "h2") {
                    siblings.add(next)
                    next = next.nextElementSibling()
                }
                val cards = mutableListOf<org.jsoup.nodes.Element>()
                for (sib in siblings) {
                    if (sib.hasClass("seasonal-anime")) {
                        cards.add(sib)
                    }
                    cards.addAll(sib.select("div.seasonal-anime"))
                }
                cards
            } else {
                // Fallback: seleciona todos os blocos .seasonal-anime da página caso o header específico varie
                doc.select("div.seasonal-anime")
            }

            for (card in elementsToParse) {
                try {
                    // Seletor CSS para o título e link do anime no card do MAL
                    val titleLink = card.select("a.link-title").first() ?: card.select("h2.h2_anime_title a").first()
                    val title = titleLink?.text()?.trim() ?: continue
                    val malUrl = titleLink.attr("href")
                    val id = extractMalId(malUrl)

                    // Seletor CSS para a imagem de capa (suporte a lazyload)
                    val imgElem = card.select("img.lazyload, img").first()
                    val imageUrl = imgElem?.attr("data-src")?.takeIf { it.isNotBlank() } ?: imgElem?.attr("src") ?: ""

                    // Seletor CSS para a nota média
                    val scoreElem = card.select("span.score").first()
                    val score = scoreElem?.text()?.trim()?.toDoubleOrNull()

                    // Cruzamento e extração de links diretos de streaming presentes no HTML do card
                    val directStreamingLinks = mutableListOf<StreamingPlatform>()
                    card.select("div.방송-link a, div.property div.link a, a.icon-streaming").forEach { link ->
                        val text = link.text().trim()
                        val href = link.attr("href")
                        if (href.isNotBlank()) {
                            directStreamingLinks.add(StreamingPlatform(name = text.ifBlank { "Streaming" }, url = href))
                        }
                    }

                    // Geração dinâmica de links de busca para as plataformas mais populares
                    val generatedLinks = generateStreamingSearchLinks(title)
                    val combinedLinks = (directStreamingLinks + generatedLinks).distinctBy { it.name }

                    animeList.add(
                        AnimeItem(
                            id = id,
                            title = title,
                            imageUrl = imageUrl,
                            malUrl = malUrl,
                            streamingLinks = combinedLinks,
                            airingDay = dayOfWeek.displayNamePt,
                            score = score ?: 0.0
                        )
                    )
                } catch (_: Exception) {
                    // Ignora itens malformados individualmente para não quebrar a listagem
                }
            }

            // Fallback geral caso a estrutura HTML mude de forma drástica
            if (animeList.isEmpty()) {
                doc.select("div.seasonal-anime").forEach { card ->
                    try {
                        val titleLink = card.select("a.link-title").first() ?: return@forEach
                        val title = titleLink.text().trim()
                        val malUrl = titleLink.attr("href")
                        val id = extractMalId(malUrl)
                        val imgElem = card.select("img").first()
                        val imageUrl = imgElem?.attr("data-src") ?: imgElem?.attr("src") ?: ""
                        val score = card.select("span.score").first()?.text()?.trim()?.toDoubleOrNull() ?: 0.0

                        animeList.add(
                            AnimeItem(
                                id = id,
                                title = title,
                                imageUrl = imageUrl,
                                malUrl = malUrl,
                                streamingLinks = generateStreamingSearchLinks(title),
                                airingDay = dayOfWeek.displayNamePt,
                                score = score
                            )
                        )
                    } catch (_: Exception) {}
                }
            }

            animeList.distinctBy { it.id }
        } catch (e: Exception) {
            throw ScrapingException("Falha ao raspar dados do MyAnimeList para o dia ${dayOfWeek.displayNamePt}: ${e.message}", e)
        }
    }

    /**
     * Extrai o ID numérico do anime a partir da URL do MyAnimeList.
     */
    private fun extractMalId(url: String): String {
        val regex = Regex("/anime/(\\d+)")
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: url.hashCode().toString()
    }

    /**
     * Gera URLs de busca formatadas para as principais plataformas de streaming.
     */
    private fun generateStreamingSearchLinks(animeTitle: String): List<StreamingPlatform> {
        val encoded = URLEncoder.encode(animeTitle, "UTF-8")
        return listOf(
            StreamingPlatform("Crunchyroll", "https://www.crunchyroll.com/search?q=$encoded"),
            StreamingPlatform("Netflix", "https://www.netflix.com/search?q=$encoded"),
            StreamingPlatform("Prime Video", "https://www.primevideo.com/search?phrase=$encoded"),
            StreamingPlatform("Bilibili Global", "https://www.bilibili.tv/en/search-result?q=$encoded")
        )
    }
}
