import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeApiService {

    // 1. Animes do Dia / Calendário de Lançamentos
    @GET("schedules")
    suspend fun getDailySchedule(
        @Query("filter") day: String? = null, // Ex: "monday", "tuesday". Se nulo, traz o dia atual
        @Query("sfw") sfw: Boolean = true     // Filtra conteúdo adulto (Explicit/Hentai)
    ): AnimeListResponse

    // 2. Animes Mais Populares
    @GET("top/anime")
    suspend fun getPopularAnimes(
        @Query("filter") filter: String = "bypopularity",
        @Query("sfw") sfw: Boolean = true,
        @Query("page") page: Int = 1
    ): AnimeListResponse

    // 3. Puxar Plataformas de Streaming do Anime (Caso o endpoint primário não preencha o array)
    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreamingPlatforms(
        @Path("id") animeId: Int
    ): StreamingLinksResponse

    companion object {
        private const val BASE_URL = "https://api.jikan.moe/v4/"

        fun create(): AnimeApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AnimeApiService::class.java)
        }
    }
}