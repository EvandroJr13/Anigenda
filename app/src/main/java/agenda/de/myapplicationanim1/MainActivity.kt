package agenda.de.myapplicationanim1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.CacheCleanupWorker
import agenda.de.myapplicationanim1.ui.MainApp
import agenda.de.myapplicationanim1.ui.theme.AgendaDeAnimesTheme
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.InterruptionLevel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.init(applicationContext)
        CacheCleanupWorker.scheduleDailyCleanup(applicationContext)

        try {
            val appDist = FirebaseAppDistribution.getInstance()
            if (!appDist.isTesterSignedIn) {
                appDist.signInTester()
            } else {
                appDist.showFeedbackNotification(
                    "Toque para enviar seu feedback do Anime Schedule",
                    InterruptionLevel.HIGH
                )
            }
        } catch (e: Exception) {
            Log.d("FirebaseAppDist", "In-app feedback notification info: ${e.message}")
        }

        val animeIdFromIntent = getAnimeIdFromIntent(intent)
        setContent {
            AgendaDeAnimesTheme {
                MainApp(initialAnimeId = animeIdFromIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val animeIdFromIntent = getAnimeIdFromIntent(intent)
        if (animeIdFromIntent != null) {
            setContent {
                AgendaDeAnimesTheme {
                    MainApp(initialAnimeId = animeIdFromIntent)
                }
            }
        }
    }

    private fun getAnimeIdFromIntent(intent: Intent?): Int? {
        val id = intent?.getIntExtra("anime_id", -1) ?: -1
        return if (id > 0) id else null
    }
}
