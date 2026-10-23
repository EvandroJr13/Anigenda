package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class MalAnimeListResponse(
    @field:Json(name = "data") val data: List<MalRankingItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class MalRankingItem(
    @field:Json(name = "node") val node: MalAnimeNode? = null
)

@JsonClass(generateAdapter = true)
data class MalAnimeSingleResponse(
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "main_picture") val mainPicture: MalPicture? = null,
    @field:Json(name = "alternative_titles") val alternativeTitles: MalAltTitles? = null,
    @field:Json(name = "start_date") val startDate: String? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "mean") val mean: Double? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "num_episodes") val numEpisodes: Int? = null,
    @field:Json(name = "broadcast") val broadcast: MalBroadcast? = null,
    @field:Json(name = "rating") val rating: String? = null
)

@JsonClass(generateAdapter = true)
data class MalAnimeNode(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "main_picture") val mainPicture: MalPicture? = null,
    @field:Json(name = "alternative_titles") val alternativeTitles: MalAltTitles? = null,
    @field:Json(name = "start_date") val startDate: String? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "mean") val mean: Double? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "num_episodes") val numEpisodes: Int? = null,
    @field:Json(name = "broadcast") val broadcast: MalBroadcast? = null,
    @field:Json(name = "rating") val rating: String? = null
) {
    fun getEnglishTitle(): String {
        val en = alternativeTitles?.en
        return en?.takeIf { it.isNotBlank() } ?: title
    }

    fun getPosterUrl(): String {
        return mainPicture?.large ?: mainPicture?.medium ?: ""
    }
}

@JsonClass(generateAdapter = true)
data class MalPicture(
    @field:Json(name = "medium") val medium: String? = null,
    @field:Json(name = "large") val large: String? = null
)

@JsonClass(generateAdapter = true)
data class MalAltTitles(
    @field:Json(name = "synonyms") val synonyms: List<String>? = emptyList(),
    @field:Json(name = "en") val en: String? = null,
    @field:Json(name = "ja") val ja: String? = null
)

@JsonClass(generateAdapter = true)
data class MalBroadcast(
    @field:Json(name = "day_of_the_week") val dayOfTheWeek: String? = null,
    @field:Json(name = "start_time") val startTime: String? = null
)

fun MalAnimeNode.toDomain(): Anime {
    val titleEng = getEnglishTitle()
    val imgUrl = getPosterUrl()
    val dayOfWeek = mapMalDayToPortuguese(broadcast?.dayOfTheWeek ?: parseDayFromMalStartDate(startDate))
    val timeStr = broadcast?.startTime ?: ""

    return Anime(
        id = id,
        title = titleEng,
        titleRomaji = title,
        titleEnglish = titleEng,
        titleNative = alternativeTitles?.ja ?: "",
        titleJapanese = alternativeTitles?.ja ?: "",
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = synopsis ?: "No synopsis available.",
        score = mean ?: 0.0,
        episodes = numEpisodes ?: 0,
        status = "Currently Airing",
        aired = startDate ?: "",
        broadcast = BroadcastInfo(
            day = dayOfWeek,
            time = timeStr,
            timezone = "JST",
            stringFormat = if (timeStr.isNotBlank()) "$dayOfWeek às $timeStr" else dayOfWeek
        ),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = startDate?.take(4)?.toIntOrNull(),
        rating = rating ?: "",
        airingDay = dayOfWeek,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = emptyList()
    )
}

fun mapMalDayToPortuguese(dayStr: String?): String {
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

fun parseDayFromMalStartDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "Segunda"
    return try {
        val datePart = dateStr.take(10)
        val localDate = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        when (localDate.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Segunda"
            java.time.DayOfWeek.TUESDAY -> "Terça"
            java.time.DayOfWeek.WEDNESDAY -> "Quarta"
            java.time.DayOfWeek.THURSDAY -> "Quinta"
            java.time.DayOfWeek.FRIDAY -> "Sexta"
            java.time.DayOfWeek.SATURDAY -> "Sábado"
            java.time.DayOfWeek.SUNDAY -> "Domingo"
        }
    } catch (_: Exception) {
        "Segunda"
    }
}
