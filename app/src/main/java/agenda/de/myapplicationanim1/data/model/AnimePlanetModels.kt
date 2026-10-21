package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimePlanetResponse(
    @field:Json(name = "results") val results: List<AnimePlanetItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnimePlanetItem(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "alt_name") val altName: String? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "rating") val rating: Double? = null,
    @field:Json(name = "airing_day") val airingDay: String? = null,
    @field:Json(name = "is_mature") val isMature: Boolean? = false
)

fun AnimePlanetItem.toDomain(): Anime {
    val titleEng = name
    val imgUrl = imageUrl ?: ""
    val dayPt = mapEnglishDayToPortuguese(airingDay)

    return Anime(
        id = id.hashCode(),
        title = titleEng,
        titleRomaji = titleEng,
        titleEnglish = titleEng,
        titleNative = altName ?: "",
        titleJapanese = altName ?: "",
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = synopsis ?: "No synopsis available.",
        score = rating ?: 0.0,
        episodes = 0,
        status = "RELEASING",
        aired = "",
        broadcast = BroadcastInfo(day = dayPt, time = "", timezone = "UTC", stringFormat = dayPt),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = null,
        rating = if (isMature == true) "Rx" else "PG-13",
        airingDay = dayPt,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = emptyList()
    )
}
