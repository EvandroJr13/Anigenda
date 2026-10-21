package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnOtakuScheduleResponse(
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "data") val data: List<AnOtakuAnimeItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnOtakuAnimeItem(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "title_english") val titleEnglish: String? = null,
    @field:Json(name = "title_japanese") val titleJapanese: String? = null,
    @field:Json(name = "cover_image") val coverImage: String? = null,
    @field:Json(name = "banner_image") val bannerImage: String? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "score") val score: Double? = null,
    @field:Json(name = "airing_day") val airingDay: String? = null,
    @field:Json(name = "airing_time") val airingTime: String? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "is_adult") val isAdult: Boolean? = false,
    @field:Json(name = "streaming_platforms") val streamingPlatforms: List<String>? = emptyList()
) {
    fun getEnglishTitle(): String {
        return titleEnglish?.takeIf { it.isNotBlank() } ?: title
    }
}

fun AnOtakuAnimeItem.toDomain(): Anime {
    val titleEng = getEnglishTitle()
    val imgUrl = coverImage ?: ""
    val dayPt = mapEnglishDayToPortuguese(airingDay)
    val timeStr = airingTime ?: ""

    return Anime(
        id = id.hashCode(),
        title = titleEng,
        titleRomaji = title,
        titleEnglish = titleEng,
        titleNative = titleJapanese ?: "",
        titleJapanese = titleJapanese ?: "",
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = synopsis ?: "No synopsis available.",
        score = score ?: 0.0,
        episodes = 0,
        status = status ?: "RELEASING",
        aired = "",
        broadcast = BroadcastInfo(
            day = dayPt,
            time = timeStr,
            timezone = "JST",
            stringFormat = if (timeStr.isNotBlank()) "$dayPt às $timeStr" else dayPt
        ),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = null,
        rating = if (isAdult == true) "Rx" else "PG-13",
        airingDay = dayPt,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = streamingPlatforms?.map { ExternalLinkInfo(site = it, url = "") } ?: emptyList()
    )
}
