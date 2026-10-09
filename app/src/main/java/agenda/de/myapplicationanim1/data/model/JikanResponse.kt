package agenda.de.myapplicationanim1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JikanListResponse<T>(
    @field:Json(name = "data") val data: List<T>? = emptyList(),
    @field:Json(name = "pagination") val pagination: PaginationDto? = null,
)

@JsonClass(generateAdapter = true)
data class JikanSingleResponse<T>(
    @field:Json(name = "data") val data: T? = null,
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @field:Json(name = "last_visible_page") val lastVisiblePage: Int? = null,
    @field:Json(name = "has_next_page") val hasNextPage: Boolean? = false,
    @field:Json(name = "current_page") val currentPage: Int? = null,
)
