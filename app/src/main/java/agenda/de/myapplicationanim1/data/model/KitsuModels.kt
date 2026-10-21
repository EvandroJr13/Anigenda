package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class KitsuAnimeListResponse(
    @field:Json(name = "data") val data: List<KitsuAnimeData>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class KitsuAnimeSingleResponse(
    @field:Json(name = "data") val data: KitsuAnimeData? = null
)

@JsonClass(generateAdapter = true)
data class KitsuAnimeData(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "type") val type: String? = null,
    @field:Json(name = "attributes") val attributes: KitsuAnimeAttributes? = null
)

@JsonClass(generateAdapter = true)
data class KitsuAnimeAttributes(
    @field:Json(name = "canonicalTitle") val canonicalTitle: String? = null,
    @field:Json(name = "titles") val titles: Map<String, String?>? = emptyMap(),
    @field:Json(name = "posterImage") val posterImage: KitsuImageWrapper? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "averageRating") val averageRating: String? = null,
    @field:Json(name = "status") val status: String? = null, // "current", "finished", "upcoming"
    @field:Json(name = "startDate") val startDate: String? = null,
    @field:Json(name = "endDate") val endDate: String? = null,
    @field:Json(name = "subtype") val subtype: String? = null,
    @field:Json(name = "ageRating") val ageRating: String? = null,
    @field:Json(name = "nsfw") val nsfw: Boolean? = false
) {
    fun getEnglishTitle(): String {
        val en = titles?.get("en") ?: titles?.get("en_us") ?: titles?.get("en_jp")
        return en?.takeIf { it.isNotBlank() } ?: canonicalTitle ?: "Unknown Title"
    }

    fun getPosterUrl(): String {
        return posterImage?.large ?: posterImage?.original ?: posterImage?.medium ?: ""
    }
}

@JsonClass(generateAdapter = true)
data class KitsuImageWrapper(
    @field:Json(name = "large") val large: String? = null,
    @field:Json(name = "original") val original: String? = null,
    @field:Json(name = "medium") val medium: String? = null
)

fun KitsuAnimeData.toDomain(): Anime {
    val attr = attributes
    val titleEng = attr?.getEnglishTitle() ?: "Unknown Title"
    val imgUrl = attr?.getPosterUrl() ?: ""
    val numericId = id.toIntOrNull() ?: id.hashCode()
    val ratingVal = attr?.averageRating?.toDoubleOrNull()?.let { it / 10.0 } ?: 0.0

    val startDateStr = attr?.startDate ?: ""
    val dayOfWeek = parseDayFromKitsuDateString(startDateStr)

    return Anime(
        id = numericId,
        title = titleEng,
        titleRomaji = attr?.titles?.get("en_jp") ?: titleEng,
        titleEnglish = titleEng,
        titleNative = attr?.titles?.get("ja_jp") ?: "",
        titleJapanese = attr?.titles?.get("ja_jp") ?: "",
        imageUrl = imgUrl,
        largeImageUrl = imgUrl,
        synopsis = attr?.synopsis ?: "No synopsis available.",
        score = ratingVal,
        episodes = 0,
        status = "Currently Airing",
        aired = startDateStr,
        broadcast = BroadcastInfo(day = dayOfWeek, time = "", timezone = "UTC", stringFormat = dayOfWeek),
        genres = emptyList(),
        studios = emptyList(),
        season = "",
        year = startDateStr.take(4).toIntOrNull(),
        rating = attr?.ageRating ?: "",
        airingDay = dayOfWeek,
        nextAiringEpisode = null,
        trailerId = null,
        trailerSite = null,
        externalLinks = emptyList()
    )
}

fun parseDayFromKitsuDateString(dateStr: String?): String {
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
