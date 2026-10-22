package agenda.de.myapplicationanim1.data.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Calendar

data class GraphQLPayload(
    @SerializedName("query") @field:Json(name = "query") val query: String,
    @SerializedName("variables") @field:Json(name = "variables") val variables: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class AniListResponse(
    @SerializedName("data") @field:Json(name = "data") val data: AniListData? = null
)

@JsonClass(generateAdapter = true)
data class AniListData(
    @SerializedName("Page") @field:Json(name = "Page") val page: AniListPage? = null,
    @SerializedName("Media") @field:Json(name = "Media") val media: AniListMedia? = null
)

@JsonClass(generateAdapter = true)
data class AniListPage(
    @SerializedName("media") @field:Json(name = "media") val media: List<AniListMedia>? = emptyList(),
    @SerializedName("airingSchedules") @field:Json(name = "airingSchedules") val airingSchedules: List<AniListAiringSchedule>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AniListAiringSchedule(
    @SerializedName("id") @field:Json(name = "id") val id: Int,
    @SerializedName("airingAt") @field:Json(name = "airingAt") val airingAt: Long,
    @SerializedName("episode") @field:Json(name = "episode") val episode: Int,
    @SerializedName("media") @field:Json(name = "media") val media: AniListMedia? = null
)

@JsonClass(generateAdapter = true)
data class AniListMedia(
    @SerializedName("id") @field:Json(name = "id") val id: Int,
    @SerializedName("title") @field:Json(name = "title") val title: AniListTitle? = null,
    @SerializedName("coverImage") @field:Json(name = "coverImage") val coverImage: AniListCoverImage? = null,
    @SerializedName("description") @field:Json(name = "description") val description: String? = null,
    @SerializedName("averageScore") @field:Json(name = "averageScore") val averageScore: Double? = null,
    @SerializedName("episodes") @field:Json(name = "episodes") val episodes: Int? = null,
    @SerializedName("status") @field:Json(name = "status") val status: String? = null,
    @SerializedName("nextAiringEpisode") @field:Json(name = "nextAiringEpisode") val nextAiringEpisode: AniListNextAiringEpisode? = null,
    @SerializedName("startDate") @field:Json(name = "startDate") val startDate: AniListFuzzyDate? = null,
    @SerializedName("genres") @field:Json(name = "genres") val genres: List<String>? = emptyList(),
    @SerializedName("trailer") @field:Json(name = "trailer") val trailer: AniListTrailer? = null,
    @SerializedName("isAdult") @field:Json(name = "isAdult") val isAdult: Boolean? = false,
    @SerializedName("externalLinks") @field:Json(name = "externalLinks") val externalLinks: List<AniListExternalLink>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AniListFuzzyDate(
    @SerializedName("year") @field:Json(name = "year") val year: Int? = null,
    @SerializedName("month") @field:Json(name = "month") val month: Int? = null,
    @SerializedName("day") @field:Json(name = "day") val day: Int? = null
)

@JsonClass(generateAdapter = true)
data class AniListTrailer(
    @SerializedName("id") @field:Json(name = "id") val id: String? = null,
    @SerializedName("site") @field:Json(name = "site") val site: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListTitle(
    @SerializedName("english") @field:Json(name = "english") val english: String? = null,
    @SerializedName("romaji") @field:Json(name = "romaji") val romaji: String? = null,
    @SerializedName("native") @field:Json(name = "native") val nativeTitle: String? = null
) {
    fun getDisplayName(): String {
        return english?.takeIf { it.isNotBlank() } ?: romaji ?: nativeTitle ?: "Unknown Title"
    }
}

@JsonClass(generateAdapter = true)
data class AniListCoverImage(
    @SerializedName("extraLarge") @field:Json(name = "extraLarge") val extraLarge: String? = null,
    @SerializedName("large") @field:Json(name = "large") val large: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListNextAiringEpisode(
    @SerializedName("airingAt") @field:Json(name = "airingAt") val airingAt: Long? = null,
    @SerializedName("episode") @field:Json(name = "episode") val episode: Int? = null
)

@JsonClass(generateAdapter = true)
data class AniListExternalLink(
    @SerializedName("site") @field:Json(name = "site") val site: String,
    @SerializedName("url") @field:Json(name = "url") val url: String? = null
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
