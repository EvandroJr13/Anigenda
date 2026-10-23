package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.BangumiDataResponse
import retrofit2.Response
import retrofit2.http.GET

interface BangumiApiService {
    @GET("gh/bangumi-data/bangumi-data@master/dist/data.json")
    suspend fun getItems(): Response<BangumiDataResponse>
}
