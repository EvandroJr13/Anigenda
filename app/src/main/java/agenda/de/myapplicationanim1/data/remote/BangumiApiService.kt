package agenda.de.myapplicationanim1.data.remote

import agenda.de.myapplicationanim1.data.model.BangumiItemDto
import retrofit2.Response
import retrofit2.http.GET

interface BangumiApiService {
    @GET("gh/bangumi-data/bangumi-data/data/items.json")
    suspend fun getItems(): Response<List<BangumiItemDto>>
}
