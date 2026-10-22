package agenda.de.myapplicationanim1.data.model

import java.util.Calendar
import java.util.Locale

data class Anime(
    val id: Int,
    val title: String,
    val titleRomaji: String = "",
    val titleEnglish: String = "",
    val titleNative: String = "",
    val titleJapanese: String = "",
    val imageUrl: String,
    val largeImageUrl: String = "",
    val synopsis: String = "",
    val score: Double = 0.0,
    val episodes: Int = 0,
    val status: String = "",
    val aired: String = "",
    val broadcast: BroadcastInfo = BroadcastInfo("", "", "", ""),
    val genres: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
    val season: String = "",
    val year: Int? = null,
    val rating: String = "",
    val airingDay: String = "Segunda",
    val nextAiringEpisode: NextAiringEpisodeInfo? = null,
    val trailerId: String? = null,
    val trailerSite: String? = null,
    val externalLinks: List<ExternalLinkInfo> = emptyList()
)

data class ExternalLinkInfo(
    val site: String,
    val url: String
)

data class BroadcastInfo(
    val day: String = "",
    val time: String = "",
    val timezone: String = "",
    val stringFormat: String = "",
)

data class NextAiringEpisodeInfo(
    val airingAt: Long,
    val episode: Int
)

fun getDayOfWeekFromTimestamp(airingAt: Long?): String {
    if (airingAt == null || airingAt <= 0L) return ""
    val calendar = Calendar.getInstance().apply {
        timeInMillis = airingAt * 1000L
    }
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Segunda"
        Calendar.TUESDAY -> "Terça"
        Calendar.WEDNESDAY -> "Quarta"
        Calendar.THURSDAY -> "Quinta"
        Calendar.FRIDAY -> "Sexta"
        Calendar.SATURDAY -> "Sábado"
        Calendar.SUNDAY -> "Domingo"
        else -> ""
    }
}

fun getDayOfWeekFromDate(year: Int?, month: Int?, day: Int?): String {
    if (year == null || month == null || day == null) return "Segunda"
    return try {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Segunda"
            Calendar.TUESDAY -> "Terça"
            Calendar.WEDNESDAY -> "Quarta"
            Calendar.THURSDAY -> "Quinta"
            Calendar.FRIDAY -> "Sexta"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> "Segunda"
        }
    } catch (_: Exception) {
        "Segunda"
    }
}
