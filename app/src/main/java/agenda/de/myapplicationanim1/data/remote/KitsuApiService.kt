package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.KitsuAnimeListResponse
import agenda.de.myapplicationanim1.data.model.KitsuAnimeSingleResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KitsuApiService {

    // Currently airing / releasing animes
    @GET("anime?filter[status]=current&page[limit]=20&sort=-userCount")
    suspend fun getCurrentlyAiringAnime(): Response<KitsuAnimeListResponse>

    // Search anime by text
    @GET("anime")
    suspend fun searchAnime(
        @Query(value = "filter[text]", encoded = true) query: String,
        @Query("page[limit]") limit: Int = 20
    ): Response<KitsuAnimeListResponse>

    // Get single anime details
    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: String
    ): Response<KitsuAnimeSingleResponse>

    companion object {
        private const val BASE_URL = "https://kitsu.io/api/edge/"

        fun create(): KitsuApiService {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor { message ->
                android.util.Log.d("KitsuHttp", message)
            }.apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            }

            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/vnd.api+json")
                        .header("Content-Type", "application/vnd.api+json")
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
                .create(KitsuApiService::class.java)
        }
    }
}
