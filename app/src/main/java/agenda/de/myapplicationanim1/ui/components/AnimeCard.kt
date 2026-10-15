package agenda.de.myapplicationanim1.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(anime) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column {
            // Image Container with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
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

                // Per-Anime Notification Toggle Button (Top-Left)
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    modifier = Modifier.align(Alignment.TopStart)
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotifEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                            contentDescription = "Notificações do Anime",
                            tint = if (isNotifEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Rating Score & Favorite Heart Badge (Top-Right)
                val scoreText = if (anime.score > 0.0) {
                    String.format(Locale.US, "%.1f", anime.score)
                } else {
                    "N/A"
                }

                Surface(
                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isFavorite = favPrefs.toggleAnimeFavorite(anime)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Amei este anime",
                                tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = scoreText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // Episode Badge e.g. "EP 10 • Hoje" (Bottom-Left)
                val epNum = anime.nextAiringEpisode?.episode ?: if (anime.episodes > 0) anime.episodes else 1
                val dayLabel = if (selectedDayName.equals("Hoje", ignoreCase = true)) "Hoje" else selectedDayName
                val episodeBadgeText = "EP $epNum • $dayLabel"

                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = episodeBadgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Info Content Section
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Studio Info
                val studioName = anime.studios.firstOrNull() ?: "Desconhecido"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = "Estúdio",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = studioName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Broadcast Info Tag
                val broadcastTag = when {
                    anime.broadcast.time.isNotEmpty() -> "${anime.broadcast.day.ifEmpty { "Transmissão" }} às ${anime.broadcast.time}"
                    anime.broadcast.stringFormat.isNotEmpty() -> anime.broadcast.stringFormat
                    else -> "Horário N/I"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Horário de exibição",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = broadcastTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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
