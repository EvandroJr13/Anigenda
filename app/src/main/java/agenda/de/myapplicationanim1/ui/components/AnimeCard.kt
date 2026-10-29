package agenda.de.myapplicationanim1.ui.components

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler
import agenda.de.myapplicationanim1.data.util.FavoritesPreferences
import agenda.de.myapplicationanim1.data.util.NotificationPreferences
import agenda.de.myapplicationanim1.ui.theme.MyApplicationAnim1Theme
import agenda.de.myapplicationanim1.ui.util.rememberNotificationPermissionState
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.util.Locale

@Composable
fun AnimeCard(
    anime: Anime,
    modifier: Modifier = Modifier,
    selectedDayName: String = "Hoje",
    showNotificationBell: Boolean = true,
    episodeBadgeText: String? = null,
    isNewRelease: Boolean = false,
    onClick: (Anime) -> Unit = {},
) {
    val context = LocalContext.current
    val permissionState = rememberNotificationPermissionState()
    val prefs = remember { NotificationPreferences(context) }
    val favPrefs = remember { FavoritesPreferences(context) }
    var isNotifEnabled by remember { mutableStateOf(prefs.isAnimeNotificationsEnabled(anime.id)) }
    var isFavorite by remember(anime.id) { mutableStateOf(favPrefs.isAnimeFavorite(anime.id)) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick(anime) },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column {
            // Poster Image Container with Reference UI Overlay Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.imageUrl.ifEmpty { anime.largeImageUrl })
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                )

                // Top-Left: Translucent Dark Circle Badge for Notification Bell (only if requested)
                if (showNotificationBell) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        IconButton(
                            onClick = {
                                val nextState = !isNotifEnabled
                                if (nextState) {
                                    permissionState.requestPermissions {
                                        isNotifEnabled = true
                                        prefs.setAnimeNotificationsEnabled(anime, true)
                                        AnimeAlarmScheduler.scheduleAlarm(context, anime)
                                    }
                                } else {
                                    isNotifEnabled = false
                                    prefs.setAnimeNotificationsEnabled(anime, false)
                                    AnimeAlarmScheduler.cancelAlarm(context, anime.id)
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isNotifEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                                contentDescription = "Notificações do Anime",
                                tint = if (isNotifEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Modern Streaming-Style "NOVO" Badge
                if (isNewRelease) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.95f),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                top = if (showNotificationBell) 48.dp else 8.dp,
                                start = 8.dp
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NOVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }

                // Top-Right: Translucent Dark Pill Badge for Heart (Favorite) & Yellow Star Rating
                val scoreText = if (anime.score > 0.0) {
                    String.format(Locale.US, "%.1f", anime.score)
                } else {
                    "N/A"
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Heart Icon
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { isFavorite = favPrefs.toggleAnimeFavorite(anime) }
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = if (isFavorite) Color(0xFFEF4444) else Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Star Rating
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFEAB308), // Gold Star
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = scoreText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Episode / Season Badge (Bottom-Left)
                val epNum = anime.nextAiringEpisode?.episode ?: if (anime.episodes > 0) anime.episodes else 1
                val totalSeasonsText = "${anime.totalSeasons} Temporada${if (anime.totalSeasons > 1) "s" else ""}"
                val defaultBadgeText = when {
                    anime.status.equals("UPCOMING", ignoreCase = true) || anime.status.equals("Not Yet Aired", ignoreCase = true) -> {
                        "Próxima: Temp. ${anime.totalSeasons}"
                    }
                    anime.status.equals("Finished", ignoreCase = true) || anime.status.equals("FINISHED", ignoreCase = true) -> {
                        totalSeasonsText
                    }
                    else -> "Ep $epNum, Temporadas ${anime.totalSeasons}"
                }
                val finalBadgeText = episodeBadgeText ?: defaultBadgeText

                Surface(
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = finalBadgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Bottom-Right: Streaming Platform Badge (Real app icon if installed, or Material Icon)
                val streamingPlatform = remember(anime.externalLinks) {
                    detectStreamingPlatform(anime.externalLinks)
                }
                if (streamingPlatform != null) {
                    val appDrawable = remember(streamingPlatform) {
                        streamingPlatform.packageNames.firstNotNullOfOrNull { pkg ->
                            getAppIcon(context, pkg)
                        }
                    }

                    val platformIcon = when (streamingPlatform.name) {
                        "Crunchyroll" -> Icons.Rounded.SmartDisplay
                        "Netflix" -> Icons.Rounded.Movie
                        "Prime Video" -> Icons.Rounded.VideoLibrary
                        "Disney+" -> Icons.Rounded.LiveTv
                        "Bilibili" -> Icons.Rounded.Tv
                        "YouTube" -> Icons.Rounded.PlayArrow
                        else -> Icons.Rounded.PlayArrow
                    }

                    Surface(
                        shape = CircleShape,
                        color = when (streamingPlatform.name) {
                            "Crunchyroll" -> Color(0xFFF97316)
                            "Netflix" -> Color(0xFF141414)
                            "Prime Video" -> Color(0xFF00A8E1)
                            "Disney+" -> Color(0xFF113CCF)
                            "Bilibili" -> Color(0xFF00AEEC)
                            "YouTube" -> Color(0xFFFF0000)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clickable {
                                openStreamingAppOrBrowser(context, streamingPlatform)
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (appDrawable != null) {
                                Image(
                                    bitmap = drawableToBitmap(appDrawable).asImageBitmap(),
                                    contentDescription = streamingPlatform.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = platformIcon,
                                    contentDescription = streamingPlatform.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Info Content Section Below Image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Broadcast Info Tag
                val broadcastTag = when {
                    anime.broadcast.time.isNotEmpty() -> "${anime.broadcast.day.ifEmpty { "Exibição" }} às ${anime.broadcast.time}"
                    anime.broadcast.stringFormat.isNotEmpty() -> anime.broadcast.stringFormat
                    else -> anime.studios.firstOrNull() ?: "Programado"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Horário de exibição",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = broadcastTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class StreamingPlatformInfo(
    val name: String,
    val url: String,
    val packageNames: List<String>
)

fun detectStreamingPlatform(externalLinks: List<agenda.de.myapplicationanim1.data.model.ExternalLinkInfo>): StreamingPlatformInfo? {
    for (link in externalLinks) {
        val siteLower = link.site.lowercase()
        val urlLower = link.url.lowercase()
        when {
            siteLower.contains("crunchyroll") || urlLower.contains("crunchyroll") -> {
                return StreamingPlatformInfo("Crunchyroll", link.url, listOf("com.crunchyroll.crunchyroid"))
            }
            siteLower.contains("netflix") || urlLower.contains("netflix") || siteLower.contains("nf") -> {
                return StreamingPlatformInfo("Netflix", link.url, listOf("com.netflix.mediaclient"))
            }
            siteLower.contains("prime") || siteLower.contains("amazon") || urlLower.contains("primevideo") || urlLower.contains("amazon") -> {
                return StreamingPlatformInfo("Prime Video", link.url, listOf("com.amazon.avod.thirdpartyclient"))
            }
            siteLower.contains("disney") || urlLower.contains("disney") -> {
                return StreamingPlatformInfo("Disney+", link.url, listOf("com.disney.disneyplus"))
            }
            siteLower.contains("bili") || urlLower.contains("bili") -> {
                return StreamingPlatformInfo("Bilibili", link.url, listOf("tv.danmaku.bili", "com.bilibili.app.in", "com.bilibili.comic"))
            }
            siteLower.contains("youtube") || urlLower.contains("youtube") || urlLower.contains("ani-one") -> {
                return StreamingPlatformInfo("YouTube", link.url, listOf("com.google.android.youtube"))
            }
            siteLower.contains("hulu") || urlLower.contains("hulu") -> {
                return StreamingPlatformInfo("Hulu", link.url, listOf("com.hulu.plus"))
            }
        }
    }
    return null
}

fun getAppIcon(context: android.content.Context, packageName: String): Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun extractYouTubeVideoId(url: String): String? {
    return when {
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        url.contains("v=") -> url.substringAfter("v=").substringBefore("&")
        else -> null
    }
}

fun openStreamingAppOrBrowser(context: android.content.Context, platform: StreamingPlatformInfo) {
    val url = platform.url
    if (url.isBlank()) return

    // 1. YouTube specific handler
    if (platform.name.equals("YouTube", ignoreCase = true) && (url.contains("youtube.com") || url.contains("youtu.be"))) {
        val videoId = extractYouTubeVideoId(url)
        val appIntent = if (!videoId.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
            return
        } catch (_: Exception) {
            try {
                context.startActivity(webIntent)
                return
            } catch (_: Exception) {}
        }
    }

    // 2. Netflix specific handler
    if (platform.name.equals("Netflix", ignoreCase = true) && url.contains("netflix.com")) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.netflix.mediaclient")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: Exception) {}
        }
    }

    // 3. General package targeting
    var openedApp = false
    for (pkg in platform.packageNames) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            openedApp = true
            break
        } catch (_: Exception) {}
    }
    if (!openedApp) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

@Preview(showBackground = true)
@Composable
fun AnimeCardPreview() {
    MyApplicationAnim1Theme {
        Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
            AnimeCard(
                anime = Anime(
                    id = 1,
                    title = "Jujutsu Kaisen Season 2",
                    titleJapanese = "呪術廻戦",
                    imageUrl = "",
                    largeImageUrl = "",
                    synopsis = "Synopsis here",
                    score = 8.82,
                    episodes = 23,
                    status = "Finished Airing",
                    aired = "2023",
                    broadcast = BroadcastInfo("Thursdays", "23:56", "JST", "Thursdays at 23:56 (JST)"),
                    genres = listOf("Action", "Fantasy"),
                    studios = listOf("MAPPA"),
                    season = "Summer",
                    year = 2023,
                    rating = "PG-13"
                )
            )
        }
    }
}
