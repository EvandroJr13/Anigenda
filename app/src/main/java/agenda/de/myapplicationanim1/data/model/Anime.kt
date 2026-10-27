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
    val externalLinks: List<ExternalLinkInfo> = emptyList(),
    val totalSeasons: Int = 1
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

fun parseSeasonNumber(vararg titles: String): Int {
    var season = 1
    for (t in titles) {
        if (t.isBlank()) continue
        val lower = t.lowercase()
        val regex = Regex("(?:season|temporada|part|partie|s)\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(lower)
        if (match != null) {
            val num = match.groupValues[1].toIntOrNull() ?: 1
            if (num in 1..25) {
                season = maxOf(season, num)
            }
        }
        if (lower.contains("2nd") || lower.contains("second") || lower.contains("2ª")) season = maxOf(season, 2)
        if (lower.contains("3rd") || lower.contains("third") || lower.contains("3ª")) season = maxOf(season, 3)
        if (lower.contains("4th") || lower.contains("4ª")) season = maxOf(season, 4)
        if (lower.contains("5th") || lower.contains("5ª")) season = maxOf(season, 5)
        if (lower.contains("6th")) season = maxOf(season, 6)
        if (lower.contains("7th")) season = maxOf(season, 7)
    }
    return season
}

fun MediaDto.toDomain(): Anime {
    return try {
        val romaji = title?.romaji ?: ""
        val english = title?.english ?: ""
        val native = title?.native ?: ""
        val bestTitle = english.ifEmpty { romaji.ifEmpty { native.ifEmpty { "Unknown Title" } } }
        val titleJapanese = native.ifEmpty { romaji }
        val totalSeasons = parseSeasonNumber(bestTitle, romaji, english)

        val imageUrl = coverImage?.extraLarge ?: coverImage?.large ?: ""
        val desc = description ?: "No synopsis available."
        val cleanSynopsis = desc
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), "")

        val rawScore = averageScore?.toDouble() ?: 0.0
        val score = if (rawScore > 10.0) rawScore / 10.0 else rawScore

        val nextEpInfo = nextAiringEpisode?.let {
            if (it.airingAt != null && it.airingAt > 0L) {
                NextAiringEpisodeInfo(airingAt = it.airingAt, episode = it.episode ?: 1)
            } else null
        }

        val airingDay = getDayOfWeekFromTimestamp(nextEpInfo?.airingAt)

        val calTime = nextEpInfo?.let {
            Calendar.getInstance().apply { timeInMillis = it.airingAt * 1000L }
        }
        val timeStr = calTime?.let {
            String.format(Locale.US, "%02d:%02d", it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
        } ?: ""

        val studioList = studios?.nodes?.mapNotNull { it.name } ?: emptyList()

        Anime(
            id = id,
            title = bestTitle,
            titleRomaji = romaji,
            titleEnglish = english,
            titleNative = native,
            titleJapanese = titleJapanese,
            imageUrl = imageUrl,
            largeImageUrl = imageUrl,
            synopsis = cleanSynopsis,
            score = score,
            episodes = episodes ?: 0,
            status = status ?: "RELEASING",
            aired = startDate?.let { "${it.year ?: "?"}-${it.month ?: "?"}-${it.day ?: "?"}" } ?: "",
            broadcast = BroadcastInfo(
                day = airingDay,
                time = timeStr,
                timezone = "UTC",
                stringFormat = if (timeStr.isNotEmpty()) "$airingDay às $timeStr" else airingDay
            ),
            genres = genres ?: emptyList(),
            studios = studioList,
            season = "",
            year = startDate?.year,
            rating = "",
            airingDay = airingDay,
            nextAiringEpisode = nextEpInfo,
            trailerId = trailer?.id,
            trailerSite = trailer?.site,
            externalLinks = externalLinks?.mapNotNull { link ->
                if (!link.site.isNullOrBlank() && !link.url.isNullOrBlank()) {
                    ExternalLinkInfo(site = link.site, url = link.url)
                } else null
            } ?: emptyList(),
            totalSeasons = totalSeasons
        )
    } catch (_: Exception) {
        Anime(
            id = id,
            title = title?.english ?: title?.romaji ?: "Unknown Title",
            imageUrl = coverImage?.large ?: "",
            totalSeasons = 1,
            synopsis = description ?: "",
            status = status ?: "RELEASING"
        )
    }
}
