package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class BangumiDataResponse(
    val items: List<BangumiItemDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BangumiItemDto(
    val id: String?,
    val title: String?,
    val type: String?,
    val lang: String?,
    val officialSite: String?,
    val begin: String?,
    val broadcast: String?,
    val weekDay: Int?,
    val sites: List<BangumiSiteDto>?,
    val image: String?,
    val summary: String?
)

@JsonClass(generateAdapter = true)
data class BangumiSiteDto(
    val site: String?,
    val id: Any?,
    val url: String?,
    val name: String?
)

fun BangumiItemDto.toDomain(): Anime {
    val titleStr = title ?: "Desconhecido"
    val numericId = id?.hashCode() ?: titleStr.hashCode()
    val imgUrl = image ?: ""

    val dayOfWeekPt = when (weekDay) {
        1 -> "Segunda"
        2 -> "Terça"
        3 -> "Quarta"
        4 -> "Quinta"
        5 -> "Sexta"
        6 -> "Sábado"
        7, 0 -> "Domingo"
        else -> parseDayFromBegin(begin) ?: "Segunda"
    }

    val officialUrl = officialSite ?: sites?.firstOrNull { it.site == "official" }?.url ?: ""
    val synopsisText = if (!summary.isNullOrBlank()) summary else "Site oficial: $officialUrl".trim()

    val yearVal = begin?.take(4)?.toIntOrNull()

    return Anime(
        id = numericId,
        title = titleStr,
        titleRomaji = titleStr,
        titleEnglish = titleStr,
        titleJapanese = titleStr,
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = synopsisText,
        score = 0.0,
        episodes = 0,
        status = "RELEASING",
        aired = begin ?: "",
        broadcast = BroadcastInfo(day = dayOfWeekPt, time = "", timezone = "JST", stringFormat = dayOfWeekPt),
        genres = listOfNotNull(type ?: "TV"),
        studios = emptyList(),
        season = "",
        year = yearVal,
        rating = "",
        airingDay = dayOfWeekPt,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null
    )
}

fun parseDayFromBegin(begin: String?): String? {
    if (begin.isNullOrBlank()) return null
    return try {
        val datePart = begin.take(10)
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
        null
    }
}
