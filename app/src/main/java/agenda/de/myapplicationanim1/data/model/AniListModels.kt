package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListResponse(
    val data: AniListData?
)

@JsonClass(generateAdapter = true)
data class AniListData(
    @Json(name = "Page")
    val page: PageDto?,
    @Json(name = "Media")
    val media: MediaDto?
)

@JsonClass(generateAdapter = true)
data class PageDto(
    val media: List<MediaDto>?
)

@JsonClass(generateAdapter = true)
data class MediaDto(
    val id: Int,
    val title: TitleDto?,
    @Json(name = "coverImage")
    val coverImage: CoverImageDto?,
    val description: String?,
    @Json(name = "averageScore")
    val averageScore: Int?,
    val episodes: Int?,
    val status: String?,
    val startDate: StartDateDto?,
    val nextAiringEpisode: NextAiringEpisodeDto?,
    val genres: List<String>?,
    val studios: StudioConnectionDto?,
    val trailer: TrailerDto?,
    val isAdult: Boolean? = false,
    @Json(name = "externalLinks")
    val externalLinks: List<ExternalLinkDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ExternalLinkDto(
    val site: String?,
    val url: String?
)

@JsonClass(generateAdapter = true)
data class TitleDto(
    val romaji: String?,
    val english: String?,
    val native: String?
)

@JsonClass(generateAdapter = true)
data class CoverImageDto(
    val extraLarge: String? = null,
    val large: String? = null
)

@JsonClass(generateAdapter = true)
data class StartDateDto(
    val year: Int?,
    val month: Int?,
    val day: Int?
)

@JsonClass(generateAdapter = true)
data class NextAiringEpisodeDto(
    val airingAt: Long?,
    val episode: Int?
)

@JsonClass(generateAdapter = true)
data class StudioConnectionDto(
    val nodes: List<StudioDto>?
)

@JsonClass(generateAdapter = true)
data class StudioDto(
    val name: String?
)

@JsonClass(generateAdapter = true)
data class TrailerDto(
    val id: String?,
    val site: String?
)
