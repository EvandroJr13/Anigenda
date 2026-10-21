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
    val titleTranslate: Map<String, List<String>>? = null,
    val type: String?,
    val lang: String?,
    val officialSite: String?,
    val begin: String?,
    val broadcast: String?,
    val weekDay: Int?,
    val sites: List<BangumiSiteDto>? = null,
    val image: String? = null,
    val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class BangumiSiteDto(
    val site: String?,
    val id: Any?,
    val url: String?,
    val name: String?
)

fun BangumiItemDto.isMatureContent(): Boolean {
    val t = type?.lowercase() ?: ""
    val s = summary?.lowercase() ?: ""
    val titleStr = title?.lowercase() ?: ""
    return t == "hentai" || t == "r18" || t == "erotica" ||
            s.contains("hentai") || s.contains("adult") || s.contains("ecchi") ||
            titleStr.contains("hentai") || titleStr.contains("erotica")
}

fun BangumiItemDto.toDomain(): Anime {
    val englishList = titleTranslate?.get("en")
        ?: titleTranslate?.get("en-US")
        ?: titleTranslate?.get("en-GB")
    val englishTitle = englishList?.firstOrNull { it.isNotBlank() }
    val titleStr = englishTitle ?: title ?: "Desconhecido"
    val numericId = id?.hashCode() ?: titleStr.hashCode()

    val bangumiSite = sites?.firstOrNull { it.site == "bangumi" }
    val bangumiId = bangumiSite?.id?.toString()

    val malSite = sites?.firstOrNull { it.site == "mal" }
    val malId = malSite?.id?.toString()

    val imgUrl = when {
        !image.isNullOrBlank() -> image
        !bangumiId.isNullOrBlank() -> "https://api.bgm.tv/v0/subjects/$bangumiId/image?type=large"
        !malId.isNullOrBlank() -> "https://cdn.myanimelist.net/images/anime/$malId.jpg"
        else -> ""
    }

    val dayOfWeekPt = when {
        weekDay != null && weekDay in 0..7 -> when (weekDay) {
            1 -> "Segunda"
            2 -> "Terça"
            3 -> "Quarta"
            4 -> "Quinta"
            5 -> "Sexta"
            6 -> "Sábado"
            7, 0 -> "Domingo"
            else -> "Segunda"
        }
        else -> parseDayFromBegin(begin) ?: "Segunda"
    }

    val officialUrl = officialSite ?: sites?.firstOrNull { it.site == "official" }?.url ?: ""
    val synopsisText = if (!summary.isNullOrBlank()) summary else "Site oficial: $officialUrl".trim()

    val yearVal = begin?.take(4)?.toIntOrNull()

    return Anime(
        id = numericId,
        title = titleStr,
        titleRomaji = title ?: titleStr,
        titleEnglish = englishTitle ?: titleStr,
        titleNative = title ?: "",
        titleJapanese = title ?: "",
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
