package agenda.de.myapplicationanim1.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
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

                if (apkAsset != null) {
                    return@withContext UpdateInfo(
                        versionName = release.tagName,
                        releaseNotes = release.body,
                        apkUrl = apkAsset.browserDownloadUrl
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error checking for updates", e)
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
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("Atualização do Aplicativo")
                .setDescription("Baixando nova versão...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            android.widget.Toast.makeText(
                context,
                "Download da atualização iniciado na barra de notificações.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error downloading update", e)
            android.widget.Toast.makeText(
                context,
                "Erro ao iniciar download: ${e.localizedMessage}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
