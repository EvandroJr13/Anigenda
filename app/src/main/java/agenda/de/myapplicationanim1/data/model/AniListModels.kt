package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Calendar

data class GraphQLPayload(
    val query: String,
    val variables: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class AniListResponse(
    @field:Json(name = "data") val data: AniListData? = null
)

@JsonClass(generateAdapter = true)
data class AniListData(
    @field:Json(name = "Page") val page: AniListPage? = null,
    @field:Json(name = "Media") val media: AniListMedia? = null
)

@JsonClass(generateAdapter = true)
data class AniListPage(
    @field:Json(name = "media") val media: List<AniListMedia>? = emptyList(),
    @field:Json(name = "airingSchedules") val airingSchedules: List<AniListAiringSchedule>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AniListAiringSchedule(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "airingAt") val airingAt: Long,
    @field:Json(name = "episode") val episode: Int,
    @field:Json(name = "media") val media: AniListMedia? = null
)

@JsonClass(generateAdapter = true)
data class AniListMedia(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "title") val title: AniListTitle? = null,
    @field:Json(name = "coverImage") val coverImage: AniListCoverImage? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "averageScore") val averageScore: Double? = null,
    @field:Json(name = "episodes") val episodes: Int? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "nextAiringEpisode") val nextAiringEpisode: AniListNextAiringEpisode? = null,
    @field:Json(name = "startDate") val startDate: AniListFuzzyDate? = null,
    @field:Json(name = "genres") val genres: List<String>? = emptyList(),
    @field:Json(name = "trailer") val trailer: AniListTrailer? = null,
    @field:Json(name = "isAdult") val isAdult: Boolean? = false,
    @field:Json(name = "externalLinks") val externalLinks: List<AniListExternalLink>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AniListFuzzyDate(
    @field:Json(name = "year") val year: Int? = null,
    @field:Json(name = "month") val month: Int? = null,
    @field:Json(name = "day") val day: Int? = null
)

@JsonClass(generateAdapter = true)
data class AniListTrailer(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "site") val site: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListTitle(
    @field:Json(name = "english") val english: String? = null,
    @field:Json(name = "romaji") val romaji: String? = null,
    @field:Json(name = "native") val nativeTitle: String? = null
) {
    fun getDisplayName(): String {
        return english?.takeIf { it.isNotBlank() } ?: romaji ?: nativeTitle ?: "Unknown Title"
    }
}

@JsonClass(generateAdapter = true)
data class AniListCoverImage(
    @field:Json(name = "extraLarge") val extraLarge: String? = null,
    @field:Json(name = "large") val large: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListNextAiringEpisode(
    @field:Json(name = "airingAt") val airingAt: Long? = null,
    @field:Json(name = "episode") val episode: Int? = null
)

@JsonClass(generateAdapter = true)
data class AniListExternalLink(
    @field:Json(name = "site") val site: String,
    @field:Json(name = "url") val url: String? = null
)

fun AniListMedia.getCalculatedWeekdayPt(): String? {
    val nextEp = nextAiringEpisode
    if (nextEp?.airingAt != null && nextEp.airingAt > 0L) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nextEp.airingAt * 1000L
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Segunda"
            Calendar.TUESDAY -> "Terça"
            Calendar.WEDNESDAY -> "Quarta"
            Calendar.THURSDAY -> "Quinta"
            Calendar.FRIDAY -> "Sexta"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> null
        }
    }

    val st = startDate
    if (st?.year != null && st.month != null && st.day != null) {
        return try {
            val cal = Calendar.getInstance().apply {
                set(st.year, st.month - 1, st.day)
            }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Segunda"
                Calendar.TUESDAY -> "Terça"
                Calendar.WEDNESDAY -> "Quarta"
                Calendar.THURSDAY -> "Quinta"
                Calendar.FRIDAY -> "Sexta"
                Calendar.SATURDAY -> "Sábado"
                Calendar.SUNDAY -> "Domingo"
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
    return null
}

fun AniListMedia.toDomain(overrideDay: String? = null): Anime {
    val titleEng = title?.getDisplayName() ?: "Unknown Title"
    val imgUrl = coverImage?.extraLarge ?: coverImage?.large ?: ""
    val scoreVal = averageScore?.let { it / 10.0 } ?: 0.0

    val ptDay = overrideDay?.takeIf { it.isNotBlank() } ?: getCalculatedWeekdayPt() ?: "Segunda"

    return Anime(
        id = id,
        title = titleEng,
        titleRomaji = title?.romaji ?: titleEng,
        titleEnglish = title?.english ?: titleEng,
        titleNative = title?.nativeTitle ?: "",
        titleJapanese = title?.nativeTitle ?: "",
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = description ?: "No synopsis available.",
        score = scoreVal,
        episodes = episodes ?: 0,
        status = status ?: "RELEASING",
        aired = startDate?.let { "${it.year ?: "?"}-${it.month ?: "?"}-${it.day ?: "?"}" } ?: "",
        broadcast = BroadcastInfo(day = ptDay, time = "", timezone = "UTC", stringFormat = ptDay),
        genres = genres ?: emptyList(),
        studios = emptyList(),
        season = "",
        year = startDate?.year,
        rating = if (isAdult == true) "Rx" else "PG-13",
        airingDay = ptDay,
        nextAiringEpisode = nextAiringEpisode?.let { NextAiringEpisodeInfo(airingAt = it.airingAt ?: 0L, episode = it.episode ?: 0) },
        trailerId = trailer?.id,
        trailerSite = trailer?.site,
        externalLinks = externalLinks?.map { ExternalLinkInfo(site = it.site, url = it.url ?: "") } ?: emptyList()
    )
}
