package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.AnimeDetailResponse
import agenda.de.myapplicationanim1.data.model.AnimeListResponse
import agenda.de.myapplicationanim1.data.model.StreamingLinksResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeApiService {

    @GET("seasons/now")
    suspend fun getCurrentSeasonAnime(
        @Query("page") page: Int = 1
    ): AnimeListResponse

    @GET("schedules")
    suspend fun getDailySchedule(
        @Query("filter") day: String? = null,
        @Query("type") type: String? = "tv"
    ): AnimeListResponse

    @GET("top/anime")
    suspend fun getPopularAnimes(
        @Query("filter") filter: String = "bypopularity",
        @Query("type") type: String? = "tv",
        @Query("page") page: Int = 1
    ): AnimeListResponse

    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreamingPlatforms(
        @Path("id") animeId: Int
    ): StreamingLinksResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") animeId: Int
    ): AnimeDetailResponse

    @GET("anime")
    suspend fun searchAnimes(
        @Query("q") query: String,
        @Query("type") type: String? = "tv"
    ): AnimeListResponse

    companion object {
        private const val BASE_URL = "https://api.jikan.moe/v4/"

        fun create(): AnimeApiService {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor { message ->
                android.util.Log.d("JikanHttp", message)
            }.apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            }

            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val newRequest = originalRequest.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(newRequest)
                }
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AnimeApiService::class.java)
        }
    }
}
