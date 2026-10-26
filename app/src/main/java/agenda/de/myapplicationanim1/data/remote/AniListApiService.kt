package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.AniListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun graphqlQuery(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<AniListResponse>
}
