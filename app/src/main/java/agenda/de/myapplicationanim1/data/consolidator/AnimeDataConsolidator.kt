package agenda.de.myapplicationanim1.data.consolidator

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// ==========================================
// 1. DATA MODEL (OBJETO DE DOMÍNIO DE SAÍDA)
// ==========================================
data class ConsolidatedAnime(
    val idBangumi: Int?,
    val idKitsu: String?,
    val idMal: Int?,
    val tituloOriginal: String,
    val tituloIngles: String,
    val tituloPortugues: String,
    val sinopseOriginal: String,
    val sinopsePortugues: String,
    val estaEmLancamento: Boolean,
    val notaMediaConsolidada: Double,
    val totalEpisodios: Int?
)

// ==========================================
// 2. MODELOS DTO PARA MAPEAMENTO DAS APIS
// ==========================================

// --- KITSU DTOs ---
data class KitsuResponse(
    @SerializedName("data") val data: List<KitsuItem>? = emptyList()
)

data class KitsuItem(
    @SerializedName("id") val id: String,
    @SerializedName("attributes") val attributes: KitsuAttributes?
)

data class KitsuAttributes(
    @SerializedName("canonicalTitle") val canonicalTitle: String?,
    @SerializedName("titles") val titles: Map<String, String?>?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("averageRating") val averageRating: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("episodeCount") val episodeCount: Int?
)

// --- JIKAN (MAL) DTOs ---
data class JikanResponse(
    @SerializedName("data") val data: List<JikanItem>? = emptyList()
)

data class JikanItem(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("airing") val airing: Boolean?,
    @SerializedName("status") val status: String?,
    @SerializedName("episodes") val episodes: Int?
)

// --- BANGUMI (v0) DTOs ---
data class BangumiSearchResponse(
    @SerializedName("list") val list: List<BangumiItem>? = emptyList()
)

data class BangumiItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("name_cn") val nameCn: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("total_episodes") val totalEpisodes: Int?
)

// ==========================================
// 3. TRADUÇÃO MOCKADA / ISOLADA
// ==========================================

/**
 * Traduz o texto fornecido para Português (PT-BR).
 * 
 * NOTA DE INTEGRAÇÃO: Em ambiente de produção real, substitua o retorno desta função
 * por uma chamada de API para um serviço de tradução como Google Cloud Translate ou DeepL API:
 * ex: return googleTranslateClient.translate(text, targetLanguage = "pt-BR")
 */
fun translateText(text: String): String {
    if (text.isBlank()) return ""
    return "[PT-BR] " + text
        .replace("The story follows", "A história acompanha")
        .replace("journey", "jornada")
        .replace("anime", "anime")
        .replace("magic", "magia")
        .replace("elves", "elfos")
}

// ==========================================
// 4. CLIENTE HTTP & CONSOLIDADOR PRINCIPAL
// ==========================================

object AnimeConsolidator {

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT = "AnimeConsolidatorApp/1.0 (evandro@animeschedule.app)"

    // --- Chamada 1: Kitsu API ---
    private suspend fun fetchKitsu(animeName: String): KitsuItem? = withContext(Dispatchers.IO) {
        val url = "https://kitsu.io/api/edge/anime?filter[text]=${encode(animeName)}&page[limit]=1"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.api+json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext null
                val result = gson.fromJson(bodyStr, KitsuResponse::class.java)
                result.data?.firstOrNull()
            } else null
        }
    }

    // --- Chamada 2: Jikan API (MAL) ---
    private suspend fun fetchJikan(animeName: String): JikanItem? = withContext(Dispatchers.IO) {
        val url = "https://api.jikan.moe/v4/anime?q=${encode(animeName)}&limit=1"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext null
                val result = gson.fromJson(bodyStr, JikanResponse::class.java)
                result.data?.firstOrNull()
            } else null
        }
    }

    // --- Chamada 3: Bangumi API (v0) ---
    private suspend fun fetchBangumi(animeName: String): BangumiItem? = withContext(Dispatchers.IO) {
        val url = "https://api.bgm.tv/v0/search/subjects"
        val jsonPayload = """
            {
                "keyword": "${escapeJson(animeName)}",
                "filter": {
                    "type": [2]
                }
            }
        """.trimIndent()

        val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext null
                val result = gson.fromJson(bodyStr, BangumiSearchResponse::class.java)
                result.list?.firstOrNull()
            } else null
        }
    }

    /**
     * Busca um anime nas 3 plataformas (Kitsu, MAL/Jikan e Bangumi) em paralelo,
     * trata falhas individuais, consolida os dados em uma única instância de ConsolidatedAnime
     * e traduz os campos necessários.
     */
    suspend fun consolidateAnime(animeName: String): ConsolidatedAnime = coroutineScope {
        // Requisições paralelas com async/await e tratamento de exceção individual
        val kitsuDeferred = async {
            try {
                fetchKitsu(animeName)
            } catch (e: Exception) {
                println("[Aviso] Falha na requisição Kitsu: ${e.localizedMessage}")
                null
            }
        }

        val jikanDeferred = async {
            try {
                fetchJikan(animeName)
            } catch (e: Exception) {
                println("[Aviso] Falha na requisição Jikan (MAL): ${e.localizedMessage}")
                null
            }
        }

        val bangumiDeferred = async {
            try {
                fetchBangumi(animeName)
            } catch (e: Exception) {
                println("[Aviso] Falha na requisição Bangumi: ${e.localizedMessage}")
                null
            }
        }

        val kitsuData = kitsuDeferred.await()
        val jikanData = jikanDeferred.await()
        val bangumiData = bangumiDeferred.await()

        // IDs
        val idBangumi = bangumiData?.id
        val idKitsu = kitsuData?.id
        val idMal = jikanData?.malId

        // Títulos
        val tituloOriginal = jikanData?.title
            ?: kitsuData?.attributes?.canonicalTitle
            ?: bangumiData?.name
            ?: animeName

        val tituloIngles = jikanData?.titleEnglish?.takeIf { it.isNotBlank() }
            ?: kitsuData?.attributes?.titles?.get("en")
            ?: kitsuData?.attributes?.canonicalTitle
            ?: bangumiData?.nameCn
            ?: tituloOriginal

        val tituloPortugues = translateText(tituloIngles)

        // Sinopse
        val sinopseOriginal = jikanData?.synopsis?.takeIf { it.isNotBlank() }
            ?: kitsuData?.attributes?.synopsis?.takeIf { it.isNotBlank() }
            ?: bangumiData?.summary
            ?: "No synopsis available."

        val sinopsePortugues = translateText(sinopseOriginal)

        // Status em Lançamento
        val estaEmLancamento = (jikanData?.airing == true || jikanData?.status?.contains("Currently Airing", ignoreCase = true) == true) ||
                (kitsuData?.attributes?.status.equals("current", ignoreCase = true))

        // Média Aritmética Consolidada das Notas
        val validScores = mutableListOf<Double>()
        jikanData?.score?.let { if (it > 0.0) validScores.add(it) }
        kitsuData?.attributes?.averageRating?.toDoubleOrNull()?.let {
            if (it > 0.0) validScores.add(it / 10.0) // Converte escala 0-100 da Kitsu para 0-10
        }
        bangumiData?.score?.let { if (it > 0.0) validScores.add(it) }

        val notaMediaConsolidada = if (validScores.isNotEmpty()) {
            val avg = validScores.average()
            Math.round(avg * 100.0) / 100.0
        } else {
            0.0
        }

        // Total de Episódios
        val totalEpisodios = jikanData?.episodes
            ?: kitsuData?.attributes?.episodeCount
            ?: bangumiData?.totalEpisodes

        ConsolidatedAnime(
            idBangumi = idBangumi,
            idKitsu = idKitsu,
            idMal = idMal,
            tituloOriginal = tituloOriginal,
            tituloIngles = tituloIngles,
            tituloPortugues = tituloPortugues,
            sinopseOriginal = sinopseOriginal,
            sinopsePortugues = sinopsePortugues,
            estaEmLancamento = estaEmLancamento,
            notaMediaConsolidada = notaMediaConsolidada,
            totalEpisodios = totalEpisodios
        )
    }

    private fun encode(value: String): String {
        return java.net.URLEncoder.encode(value, "UTF-8")
    }

    private fun escapeJson(value: String): String {
        return value.replace("\"", "\\\"")
    }
}

// ==========================================
// 5. FUNÇÃO MAIN DE TESTE E DEMONSTRAÇÃO
// ==========================================
fun main() = runBlocking {
    val searchTerms = listOf("Sousou no Frieren", "Chainsaw Man")

    for (term in searchTerms) {
        println("\n==================================================")
        println("Iniciando consolidação paralela para: '$term'")
        println("==================================================")

        val consolidated = AnimeConsolidator.consolidateAnime(term)

        println("\n--- RESULTADO CONSOLIDADO ---")
        println("ID Bangumi            : ${consolidated.idBangumi ?: "N/I"}")
        println("ID Kitsu              : ${consolidated.idKitsu ?: "N/I"}")
        println("ID MAL (MyAnimeList)  : ${consolidated.idMal ?: "N/I"}")
        println("Título Original       : ${consolidated.tituloOriginal}")
        println("Título em Inglês      : ${consolidated.tituloIngles}")
        println("Título em Português   : ${consolidated.tituloPortugues}")
        println("Está em Lançamento?   : ${if (consolidated.estaEmLancamento) "SIM ✅" else "NÃO ❌"}")
        println("Nota Média Consolidada : ${consolidated.notaMediaConsolidada} / 10.0")
        println("Total de Episódios    : ${consolidated.totalEpisodios ?: "Em exibição / N/I"}")
        println("\n--- SINOPSE ORIGINAL (EN) ---")
        println(consolidated.sinopseOriginal.take(200) + "...")
        println("\n--- SINOPSE TRADUZIDA (PT) ---")
        println(consolidated.sinopsePortugues.take(200) + "...")
    }
}
