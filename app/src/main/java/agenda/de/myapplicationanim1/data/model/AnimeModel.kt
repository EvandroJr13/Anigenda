package agenda.de.myapplicationanim1.data.model

import com.google.gson.annotations.SerializedName

// Wrapper base da resposta da API
data class AnimeListResponse(
    @SerializedName("data") val data: List<AnimeModel>? = emptyList()
)

data class AnimeDetailResponse(
    @SerializedName("data") val data: AnimeModel? = null
)

data class StreamingLinksResponse(
    @SerializedName("data") val data: List<StreamingPlatform>? = emptyList()
)

data class AnimeModel(
    @SerializedName("mal_id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("images") val images: ImageWrapper?,
    @SerializedName("broadcast") val broadcast: JikanBroadcastInfo?,
    @SerializedName("rating") val rating: String?, // Ex: "PG-13", "R - 17+", "Rx - Hentai"
    @SerializedName("streaming") val streaming: List<StreamingPlatform>?, // Plataformas do anime
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("episodes") val episodes: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("airing") val airing: Boolean? = null
) {
    // Retorna o nome em inglês; se não houver tradução oficial, usa o nome padrão
    fun getDisplayName(): String = titleEnglish?.takeIf { it.isNotBlank() } ?: title

    // Retorna a URL da capa em alta resolução (ou fallback para a padrão)
    fun getImageUrl(): String? = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl
}

data class ImageWrapper(
    @SerializedName("jpg") val jpg: ImageType?
)

data class ImageType(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?
)

data class JikanBroadcastInfo(
    @SerializedName("day") val day: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("string") val fullBroadcast: String? // Ex: "Sundays at 17:00 (JST)"
)

data class StreamingPlatform(
    @SerializedName("name") val name: String, // Ex: "Crunchyroll", "Netflix", "Disney+"
    @SerializedName("url") val url: String? = null
)

fun AnimeModel.toDomain(matchedPlatforms: List<String> = emptyList()): Anime {
    val titleEng = getDisplayName()
    val imgUrl = getImageUrl() ?: ""
    val dayStr = broadcast?.day ?: "Segunda"
    val timeStr = broadcast?.time ?: ""
    val fullBroadcastStr = broadcast?.fullBroadcast ?: if (timeStr.isNotBlank()) "$dayStr às $timeStr" else dayStr

    val streamingLinks = (streaming?.map { ExternalLinkInfo(site = it.name, url = it.url ?: "") } ?: emptyList()) +
            matchedPlatforms.map { ExternalLinkInfo(site = it, url = "") }

    val ptDay = mapEnglishDayToPortuguese(dayStr)

    return Anime(
        id = id,
        title = titleEng,
        titleRomaji = title,
        titleEnglish = titleEnglish ?: titleEng,
        titleNative = title,
        titleJapanese = title,
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = synopsis ?: "No synopsis available.",
        score = score ?: 0.0,
        episodes = episodes ?: 0,
        status = status ?: "Currently Airing",
        aired = "",
        broadcast = BroadcastInfo(
            day = ptDay,
            time = timeStr,
            timezone = broadcast?.timezone ?: "JST",
            stringFormat = fullBroadcastStr
        ),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = null,
        rating = rating ?: "",
        airingDay = ptDay,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = streamingLinks
    )
}

fun mapEnglishDayToPortuguese(dayStr: String?): String {
    if (dayStr.isNullOrBlank()) return "Segunda"
    val lower = dayStr.lowercase()
    return when {
        lower.contains("monday") || lower.contains("segunda") -> "Segunda"
        lower.contains("tuesday") || lower.contains("terça") -> "Terça"
        lower.contains("wednesday") || lower.contains("quarta") -> "Quarta"
        lower.contains("thursday") || lower.contains("quinta") -> "Quinta"
        lower.contains("friday") || lower.contains("sexta") -> "Sexta"
        lower.contains("saturday") || lower.contains("sábado") -> "Sábado"
        lower.contains("sunday") || lower.contains("domingo") -> "Domingo"
        else -> "Segunda"
    }
}

fun mapPortugueseDayToEnglishFilter(dayPt: String?): String? {
    if (dayPt.isNullOrBlank()) return null
    val lower = dayPt.lowercase()
    return when {
        lower.contains("segunda") -> "monday"
        lower.contains("terça") -> "tuesday"
        lower.contains("quarta") -> "wednesday"
        lower.contains("quinta") -> "thursday"
        lower.contains("sexta") -> "friday"
        lower.contains("sábado") -> "saturday"
        lower.contains("domingo") -> "sunday"
        else -> null
    }
}
