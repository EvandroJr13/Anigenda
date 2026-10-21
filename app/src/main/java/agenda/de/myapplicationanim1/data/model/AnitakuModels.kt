package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnitakuResponse(
    @field:Json(name = "currentPage") val currentPage: Int? = 1,
    @field:Json(name = "hasNextPage") val hasNextPage: Boolean? = false,
    @field:Json(name = "results") val results: List<AnitakuAnimeItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnitakuAnimeItem(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "image") val image: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "episodeId") val episodeId: String? = null,
    @field:Json(name = "episodeNumber") val episodeNumber: Int? = null,
    @field:Json(name = "subOrDub") val subOrDub: String? = null,
    @field:Json(name = "releaseDate") val releaseDate: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "status") val status: String? = null
)

fun AnitakuAnimeItem.toDomain(): Anime {
    val titleEng = title
    val imgUrl = image ?: ""
    val dayPt = "Segunda"

    return Anime(
        id = id.hashCode(),
        title = titleEng,
        titleRomaji = titleEng,
        titleEnglish = titleEng,
        titleNative = titleEng,
        titleJapanese = titleEng,
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = description ?: "No synopsis available.",
        score = 0.0,
        episodes = episodeNumber ?: 0,
        status = status ?: "RELEASING",
        aired = releaseDate ?: "",
        broadcast = BroadcastInfo(day = dayPt, time = "", timezone = "UTC", stringFormat = dayPt),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = releaseDate?.take(4)?.toIntOrNull(),
        rating = "PG-13",
        airingDay = dayPt,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = listOf(ExternalLinkInfo(site = "Anitaku", url = url ?: ""))
    )
}
