package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeDto(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "title_english") val titleEnglish: String? = null,
    @field:Json(name = "title_japanese") val titleJapanese: String? = null,
    @field:Json(name = "images") val images: ImageTypesDto? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "score") val score: Double? = null,
    @field:Json(name = "episodes") val episodes: Int? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "aired") val aired: AiredDto? = null,
    @field:Json(name = "broadcast") val broadcast: BroadcastDto? = null,
    @field:Json(name = "genres") val genres: List<GenericResourceDto>? = emptyList(),
    @field:Json(name = "studios") val studios: List<GenericResourceDto>? = emptyList(),
    @field:Json(name = "season") val season: String? = null,
    @field:Json(name = "year") val year: Int? = null,
    @field:Json(name = "rating") val rating: String? = null,
)

@JsonClass(generateAdapter = true)
data class ImageTypesDto(
    @field:Json(name = "jpg") val jpg: ImageUrlDto? = null,
    @field:Json(name = "webp") val webp: ImageUrlDto? = null,
)

@JsonClass(generateAdapter = true)
data class ImageUrlDto(
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "small_image_url") val smallImageUrl: String? = null,
    @field:Json(name = "large_image_url") val largeImageUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiredDto(
    @field:Json(name = "from") val from: String? = null,
    @field:Json(name = "to") val to: String? = null,
    @field:Json(name = "string") val string: String? = null,
)

@JsonClass(generateAdapter = true)
data class BroadcastDto(
    @field:Json(name = "day") val day: String? = null,
    @field:Json(name = "time") val time: String? = null,
    @field:Json(name = "timezone") val timezone: String? = null,
    @field:Json(name = "string") val string: String? = null,
)

@JsonClass(generateAdapter = true)
data class GenericResourceDto(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "type") val type: String? = null,
    @field:Json(name = "url") val url: String? = null,
)
