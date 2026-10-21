package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.MalAnimeListResponse
import agenda.de.myapplicationanim1.data.model.MalAnimeSingleResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MalApiService {

    // Currently airing ranking from MyAnimeList v2
    @GET("anime/ranking")
    suspend fun getAiringAnimeRanking(
        @Query("ranking_type") rankingType: String = "airing",
        @Query("limit") limit: Int = 50,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,synopsis,mean,status,num_episodes,broadcast,rating"
    ): Response<MalAnimeListResponse>

    // Search anime on MyAnimeList v2
    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,synopsis,mean,status,num_episodes,broadcast,rating"
    ): Response<MalAnimeListResponse>

    // Get single anime details on MyAnimeList v2
    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: Int,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,synopsis,mean,status,num_episodes,broadcast,rating"
    ): Response<MalAnimeSingleResponse>

    companion object {
        private const val BASE_URL = "https://api.myanimelist.net/v2/"
        private const val CLIENT_ID = "610ba5bc002c01d9338b843372f7e028"

        fun create(): MalApiService {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor { message ->
                android.util.Log.d("MalHttp", message)
            }.apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            }

            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("X-MAL-CLIENT-ID", CLIENT_ID)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MalApiService::class.java)
        }
    }
}
