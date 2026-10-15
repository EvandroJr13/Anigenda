package agenda.de.myapplicationanim1.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String?,
    val apkUrl: String
)

class UpdateManager(
    private val context: Context,
    private val repoOwner: String,
    private val repoName: String
) {
    private val api: GithubReleaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GithubReleaseApi::class.java)
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val release = api.getLatestRelease(repoOwner, repoName)
            val remoteTag = release.tagName.removePrefix("v").trim()
            val currentVersion = getAppVersionName()

            if (isVersionNewer(currentVersion, remoteTag)) {
                val apkAsset = release.assets?.find { it.name.endsWith(".apk", ignoreCase = true) }
                    ?: release.assets?.firstOrNull()

                val downloadUrl = apkAsset?.browserDownloadUrl 
                    ?: release.htmlUrl 
                    ?: "https://github.com/$repoOwner/$repoName/releases/latest"

                return@withContext UpdateInfo(
                    versionName = release.tagName,
                    releaseNotes = release.body,
                    apkUrl = downloadUrl
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error checking for updates from GitHub", e)
        }
        return@withContext null
    }

    private fun getAppVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    private fun isVersionNewer(current: String, remote: String): Boolean {
        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val c = currentParts.getOrNull(i) ?: 0
                val r = remoteParts.getOrNull(i) ?: 0
                if (r > c) return true
                if (r < c) return false
            }
        } catch (e: Exception) {
            return remote != current
        }
        return false
    }

    fun downloadAndInstall(apkUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error opening update URL", e)
            android.widget.Toast.makeText(
                context,
                "Erro ao abrir link de download: ${e.localizedMessage}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
