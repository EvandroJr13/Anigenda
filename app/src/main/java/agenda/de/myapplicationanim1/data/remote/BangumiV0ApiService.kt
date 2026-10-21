package agenda.de.myapplicationanim1.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class BangumiSearchRequest(
    val keyword: String,
    val filter: BangumiSearchFilter = BangumiSearchFilter()
)

@JsonClass(generateAdapter = true)
data class BangumiSearchFilter(
    val type: List<Int> = listOf(2) // 2 = anime
)

@JsonClass(generateAdapter = true)
data class BangumiSearchResponse(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "list") val list: List<BangumiSubjectSummary>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BangumiSubjectSummary(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "name_cn") val nameCn: String?,
    @Json(name = "summary") val summary: String?
)

@JsonClass(generateAdapter = true)
data class BangumiSubjectDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "name_cn") val nameCn: String?,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "rank") val rank: Int? = null,
    @Json(name = "total_episodes") val totalEpisodes: Int? = null
)

@JsonClass(generateAdapter = true)
data class BangumiEpisodeResponse(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "data") val data: List<BangumiEpisodeItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BangumiEpisodeItem(
    @Json(name = "id") val id: Int,
    @Json(name = "sort") val sort: Float? = null,
    @Json(name = "name") val name: String?,
    @Json(name = "name_cn") val nameCn: String?
)

interface BangumiV0ApiService {
    @POST("v0/search/subjects")
    suspend fun searchSubjects(
        @Body request: BangumiSearchRequest
    ): Response<BangumiSearchResponse>

    @GET("v0/subjects/{id}")
    suspend fun getSubjectDetails(
        @Path("id") subjectId: Int
    ): Response<BangumiSubjectDetail>

    @GET("v0/episodes")
    suspend fun getEpisodes(
        @Query("subject_id") subjectId: Int,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<BangumiEpisodeResponse>

    companion object {
        private const val BASE_URL = "https://api.bgm.tv/"

        fun create(): BangumiV0ApiService {
            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", "AnimeScheduleApp/1.0 (evandro@animeschedule.app)")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
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
                .create(BangumiV0ApiService::class.java)
        }
    }
}
