package agenda.de.myapplicationanim1.update

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface GithubReleaseApi {
    @Headers("User-Agent: AnimeScheduleApp")
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubReleaseDto
}
