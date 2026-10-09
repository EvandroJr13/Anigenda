package agenda.de.myapplicationanim1.data.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import agenda.de.myapplicationanim1.MainActivity

object AnimeNotificationManager {
    private const val CHANNEL_ID = "anime_release_channel"
    private const val CHANNEL_NAME = "Lançamentos de Animes"
    private const val CHANNEL_DESCRIPTION = "Notificações para episódios de animes agendados"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReleaseNotification(context: Context, animeId: Int, animeTitle: String, episodeNumber: Int = 1) {
        createNotificationChannel(context)
        val prefs = NotificationPreferences(context)
        if (!prefs.isGlobalNotificationsEnabled) return
        if (!prefs.isAnimeNotificationsEnabled(animeId)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            animeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Novo Episódio Disponível! 📺")
            .setContentText("O episódio $episodeNumber de '$animeTitle' está no ar ou agendado.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("O episódio $episodeNumber de '$animeTitle' foi lançado ou está agendado para transmissão hoje. Não perca!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(animeId, notification)
    }
}
