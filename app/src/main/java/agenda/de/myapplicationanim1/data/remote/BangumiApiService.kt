package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.BangumiDataResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface BangumiApiService {
    @GET("gh/bangumi-data/bangumi-data@master/dist/data.json")
    suspend fun getItems(): Response<BangumiDataResponse>

    companion object {
        private const val BASE_URL = "https://cdn.jsdelivr.net/"

        fun create(): BangumiApiService {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(BangumiApiService::class.java)
        }
    }
}
