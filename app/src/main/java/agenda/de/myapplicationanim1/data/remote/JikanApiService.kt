package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.AnimeDto
import agenda.de.myapplicationanim1.data.model.JikanListResponse
import agenda.de.myapplicationanim1.data.model.JikanSingleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {

    @GET("schedules")
    suspend fun getSchedules(
        @Query("page") page: Int? = 1,
        @Query("sfw") sfw: Boolean = true,
    ): Response<JikanListResponse<AnimeDto>>

    @GET("schedules/{day}")
    suspend fun getScheduleByDay(
        @Path("day") day: String,
        @Query("page") page: Int? = 1,
        @Query("sfw") sfw: Boolean = true,
    ): Response<JikanListResponse<AnimeDto>>

    @GET("seasons/{year}/{season}")
    suspend fun getSeasonalAnime(
        @Path("year") year: Int,
        @Path("season") season: String,
        @Query("page") page: Int? = 1,
        @Query("sfw") sfw: Boolean = true,
    ): Response<JikanListResponse<AnimeDto>>

    @GET("seasons/now")
    suspend fun getCurrentSeasonAnime(
        @Query("page") page: Int? = 1,
        @Query("sfw") sfw: Boolean = true,
    ): Response<JikanListResponse<AnimeDto>>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int? = 1,
        @Query("sfw") sfw: Boolean = true,
    ): Response<JikanListResponse<AnimeDto>>

    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: Int,
    ): Response<JikanSingleResponse<AnimeDto>>
}
