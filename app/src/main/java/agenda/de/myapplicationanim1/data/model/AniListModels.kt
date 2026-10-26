package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListResponse(
    @Json(name = "data")
    val data: AniListData? = null
)

@JsonClass(generateAdapter = true)
data class AniListData(
    @Json(name = "Page")
    val page: PageDto? = null,
    @Json(name = "Media")
    val media: MediaDto? = null
)

@JsonClass(generateAdapter = true)
data class PageDto(
    @Json(name = "media")
    val media: List<MediaDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class MediaDto(
    @Json(name = "id")
    val id: Int = 0,
    @Json(name = "title")
    val title: TitleDto? = null,
    @Json(name = "coverImage")
    val coverImage: CoverImageDto? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "averageScore")
    val averageScore: Int? = null,
    @Json(name = "episodes")
    val episodes: Int? = null,
    @Json(name = "status")
    val status: String? = null,
    @Json(name = "startDate")
    val startDate: StartDateDto? = null,
    @Json(name = "nextAiringEpisode")
    val nextAiringEpisode: NextAiringEpisodeDto? = null,
    @Json(name = "genres")
    val genres: List<String>? = emptyList(),
    @Json(name = "studios")
    val studios: StudioConnectionDto? = null,
    @Json(name = "trailer")
    val trailer: TrailerDto? = null,
    @Json(name = "isAdult")
    val isAdult: Boolean? = false,
    @Json(name = "externalLinks")
    val externalLinks: List<ExternalLinkDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ExternalLinkDto(
    @Json(name = "site")
    val site: String? = null,
    @Json(name = "url")
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class TitleDto(
    @Json(name = "romaji")
    val romaji: String? = null,
    @Json(name = "english")
    val english: String? = null,
    @Json(name = "native")
    val native: String? = null
)

@JsonClass(generateAdapter = true)
data class CoverImageDto(
    @Json(name = "extraLarge")
    val extraLarge: String? = null,
    @Json(name = "large")
    val large: String? = null
)

@JsonClass(generateAdapter = true)
data class StartDateDto(
    @Json(name = "year")
    val year: Int? = null,
    @Json(name = "month")
    val month: Int? = null,
    @Json(name = "day")
    val day: Int? = null
)

@JsonClass(generateAdapter = true)
data class NextAiringEpisodeDto(
    @Json(name = "airingAt")
    val airingAt: Long? = null,
    @Json(name = "episode")
    val episode: Int? = null
)

@JsonClass(generateAdapter = true)
data class StudioConnectionDto(
    @Json(name = "nodes")
    val nodes: List<StudioDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class StudioDto(
    @Json(name = "name")
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class TrailerDto(
    @Json(name = "id")
    val id: String? = null,
    @Json(name = "site")
    val site: String? = null
)
