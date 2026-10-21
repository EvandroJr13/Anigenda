package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.AnitakuInfoResponse
import agenda.de.myapplicationanim1.data.model.AnitakuResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnitakuApiService {

    @GET("recent-episodes")
    suspend fun getRecentEpisodes(
        @Query("page") page: Int = 1
    ): Response<AnitakuResponse>

    @GET("top-airing")
    suspend fun getTopAiring(
        @Query("page") page: Int = 1
    ): Response<AnitakuResponse>

    @GET("popular")
    suspend fun getPopular(
        @Query("page") page: Int = 1
    ): Response<AnitakuResponse>

    @GET("search/{key}")
    suspend fun search(
        @Path("key") query: String
    ): Response<AnitakuResponse>

    @GET("info/{id}")
    suspend fun getInfo(
        @Path("id") id: String
    ): Response<AnitakuInfoResponse>

    companion object {
        private const val BASE_URL = "https://gogoanime-api-eta.vercel.app/"

        fun create(): AnitakuApiService {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor { message ->
                android.util.Log.d("AnitakuHttp", message)
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
                .create(AnitakuApiService::class.java)
        }
    }
}
