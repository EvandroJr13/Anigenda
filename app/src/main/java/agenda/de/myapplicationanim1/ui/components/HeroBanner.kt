package agenda.de.myapplicationanim1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler
import agenda.de.myapplicationanim1.data.util.NotificationPreferences
import agenda.de.myapplicationanim1.ui.util.rememberNotificationPermissionState
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.util.Locale

@Composable
fun HeroBanner(
    anime: Anime,
    modifier: Modifier = Modifier,
    onAnimeClick: (Anime) -> Unit = {},
) {
    val context = LocalContext.current
    val permissionState = rememberNotificationPermissionState()
    val prefs = remember { NotificationPreferences(context) }
    var isNotifEnabled by remember { mutableStateOf(prefs.isAnimeNotificationsEnabled(anime.id)) }
    var isFavorite by remember { mutableStateOf(prefs.isAnimeNotificationsEnabled(anime.id)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop Image
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.largeImageUrl.ifEmpty { anime.imageUrl })
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
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            )

            // Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.50f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Bottom Information Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // "Trending:" Tag
                Text(
                    text = "Trending:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                // Large Title
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Subtitle / Info
                val broadcastString = when {
                    anime.broadcast.time.isNotEmpty() -> "Exibição hoje às ${anime.broadcast.time}"
                    anime.broadcast.stringFormat.isNotEmpty() -> anime.broadcast.stringFormat
                    else -> "Lançamento em Alta"
                }
                Text(
                    text = broadcastString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // "Watch Now" White Capsule Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onAnimeClick(anime) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFA855F7)
                        ),
                        shape = CircleShape,
                        modifier = Modifier.height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assistir Agora",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFA855F7)
                        )
                    }

                    // Action Icons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Heart Icon
                        FilledTonalIconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                prefs.setAnimeNotificationsEnabled(anime, isFavorite)
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.45f),
                                contentColor = if (isFavorite) Color(0xFFEF4444) else Color.White
                            ),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorito",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bell Icon
                        FilledTonalIconButton(
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
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.45f),
                                contentColor = if (isNotifEnabled) Color(0xFFA855F7) else Color.White
                            ),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isNotifEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                                contentDescription = "Notificação",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
