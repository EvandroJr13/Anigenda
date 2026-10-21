package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.AnimePlanetResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimePlanetApiService {

    @GET("anime/seasonal")
    suspend fun getSeasonalAnime(
        @Query("day") day: String? = null
    ): Response<AnimePlanetResponse>

    @GET("anime/search")
    suspend fun searchAnime(
        @Query("q") query: String
    ): Response<AnimePlanetResponse>

    companion object {
        private const val BASE_URL = "https://www.anime-planet.com/api/"

        fun create(): AnimePlanetApiService {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor { message ->
                android.util.Log.d("AnimePlanetHttp", message)
            }.apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            }

            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
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
                .create(AnimePlanetApiService::class.java)
        }
    }
}
