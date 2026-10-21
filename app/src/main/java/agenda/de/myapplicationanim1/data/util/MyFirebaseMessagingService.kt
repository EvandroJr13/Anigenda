package agenda.de.myapplicationanim1.data.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "From: ${remoteMessage.from}")

        remoteMessage.notification?.let { notification ->
            Log.d("FCM_MESSAGE", "Message Notification Body: ${notification.body}")
            AnimeNotificationManager.showReleaseNotification(
                context = applicationContext,
                animeId = -1,
                animeTitle = notification.title ?: "Anime Schedule",
                episodeNumber = 1
            )
        }
    }
}
