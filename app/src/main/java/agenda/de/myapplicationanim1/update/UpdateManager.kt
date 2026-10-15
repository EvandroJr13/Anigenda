package agenda.de.myapplicationanim1.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String?,
    val apkUrl: String
)

sealed interface UpdateCheckResult {
    data class NewUpdate(val info: UpdateInfo) : UpdateCheckResult
    object AlreadyLatest : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

class UpdateManager(
    private val context: Context,
    private val repoOwner: String,
    private val repoName: String
) {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val api: GithubReleaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GithubReleaseApi::class.java)
    }

    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
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

                return@withContext UpdateCheckResult.NewUpdate(
                    UpdateInfo(
                        versionName = release.tagName,
                        releaseNotes = release.body,
                        apkUrl = downloadUrl
                    )
                )
            } else {
                return@withContext UpdateCheckResult.AlreadyLatest
            }
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                404 -> "Repositório ou release não encontrada no GitHub (verifique se o repositório é público)."
                403 -> "Limite de requisições do GitHub atingido ou acesso negado."
                else -> "Erro HTTP ${e.code()}: ${e.message()}"
            }
            Log.e("UpdateManager", msg, e)
            return@withContext UpdateCheckResult.Error(msg)
        } catch (e: Exception) {
            val msg = "Falha ao verificar atualização: ${e.localizedMessage ?: e.message}"
            Log.e("UpdateManager", msg, e)
            return@withContext UpdateCheckResult.Error(msg)
        }
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
