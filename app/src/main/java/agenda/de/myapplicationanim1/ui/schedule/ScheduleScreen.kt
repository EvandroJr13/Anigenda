package agenda.de.myapplicationanim1.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler
import agenda.de.myapplicationanim1.data.util.NotificationPreferences
import agenda.de.myapplicationanim1.ui.components.AnimeCard
import agenda.de.myapplicationanim1.ui.components.ApiSourceFooter
import agenda.de.myapplicationanim1.ui.components.EmptyStateView
import agenda.de.myapplicationanim1.ui.components.ErrorStateView
import agenda.de.myapplicationanim1.ui.components.FrontPageDialog
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
import agenda.de.myapplicationanim1.ui.components.SectionHeader
import agenda.de.myapplicationanim1.ui.components.ShimmerGridLoading
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily
import agenda.de.myapplicationanim1.ui.theme.MyApplicationAnim1Theme
import agenda.de.myapplicationanim1.ui.util.rememberNotificationPermissionState
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = viewModel(),
    onAnimeClick: (Anime) -> Unit = {},
    showFrontPageOnLaunch: Boolean = true,
    onFrontPageShown: () -> Unit = {}
) {
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScheduleContent(
        selectedDay = selectedDay,
        uiState = uiState,
        onDaySelected = { viewModel.selectDay(it) },
        onRetry = { viewModel.retry() },
        onAnimeClick = onAnimeClick,
        showFrontPageOnLaunch = showFrontPageOnLaunch,
        onFrontPageShown = onFrontPageShown,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    selectedDay: ScheduleDay,
    uiState: ScheduleUiState,
    onDaySelected: (ScheduleDay) -> Unit,
    onRetry: () -> Unit,
    onAnimeClick: (Anime) -> Unit,
    showFrontPageOnLaunch: Boolean = true,
    onFrontPageShown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showFrontPage by remember { mutableStateOf(showFrontPageOnLaunch) }
    val gridState = rememberLazyGridState()

    // Front Page / Destaque do Dia Dialog on launch
    if (showFrontPage && uiState is ScheduleUiState.Success) {
        val animeList = uiState.animeList
        if (animeList.isNotEmpty()) {
            val featuredAnime = animeList.maxByOrNull { it.score } ?: animeList.firstOrNull()
            if (featuredAnime != null) {
                FrontPageDialog(
                    anime = featuredAnime,
                    onDismiss = {
                        showFrontPage = false
                        onFrontPageShown()
                    },
                    onViewDetails = { anime ->
                        showFrontPage = false
                        onFrontPageShown()
                        onAnimeClick(anime)
                    }
                )
            }
        }
    }

    val featuredAnime = if (uiState is ScheduleUiState.Success) {
        val list = uiState.animeList
        list.maxByOrNull { it.score } ?: list.firstOrNull()
    } else null

    // Scroll offset calculation
    val scrollOffset by remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex == 0) {
                gridState.firstVisibleItemScrollOffset.toFloat()
            } else {
                1000f
            }
        }
    }

    // Top Header Background Alpha: 0f when at top, transitions to 1f when scrolling down
    val topBarAlpha by remember {
        derivedStateOf {
            (scrollOffset / 120f).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Scrollable Main Content Area
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = uiState is ScheduleUiState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(110.dp))
                    ShimmerGridLoading()
                }
            }

            AnimatedVisibility(
                visible = uiState is ScheduleUiState.Error,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState is ScheduleUiState.Error) {
                    Column {
                        Spacer(modifier = Modifier.height(110.dp))
                        ErrorStateView(
                            errorMessage = uiState.message,
                            onRetry = onRetry
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState is ScheduleUiState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState is ScheduleUiState.Success) {
                    val animeList = uiState.animeList
                    if (animeList.isEmpty()) {
                        Column {
                            Spacer(modifier = Modifier.height(110.dp))
                            EmptyStateView(message = "Nenhum anime programado para ${selectedDay.displayNamePt}.")
                        }
                    } else {
                        val sectionTitle = if (selectedDay == ScheduleDay.today()) "Seleção de Hoje" else "Seleção do Dia"

                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Item 1: Full-Width Immersive Hero Header (Covers all the way to the top when scrollOffset == 0)
                            if (featuredAnime != null) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    FullTopHeroHeader(
                                        anime = featuredAnime,
                                        scrollOffset = scrollOffset,
                                        onAnimeClick = onAnimeClick,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Spacer(modifier = Modifier.height(110.dp))
                                }
                            }

                            // Item 2: Section Header ("| Seleção de Hoje")
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(
                                    title = sectionTitle,
                                    subtitle = "Programação de ${selectedDay.displayNamePt}",
                                    itemCount = animeList.size,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }

                            // Item 3: Anime Schedule Grid Cards
                            items(animeList, key = { it.id }) { anime ->
                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    AnimeCard(
                                        anime = anime,
                                        selectedDayName = if (selectedDay == ScheduleDay.today()) "Hoje" else selectedDay.displayNamePt,
                                        onClick = onAnimeClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Fixed Overlay Top Header (Logo + Day Filter Tabs)
        // Background is 100% transparent when at top, and transitions smoothly to solid dark when scrolling down!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = topBarAlpha)
                )
        ) {
            // Top Bar Row: "Anigenda" Logo + Notification Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (uiState is ScheduleUiState.Success) {
                            showFrontPage = true
                        }
                    }
                ) {
                    Text(
                        text = "Ani",
                        fontFamily = ChewyFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "genda",
                        fontFamily = ChewyFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7)
                    )
                }

                GlobalNotificationAction()
            }

            // Fixed Day Filter Capsule Pills Row ("SEGUNDA", "TERÇA"...)
            Surface(
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ScheduleDay.entries.toTypedArray(), key = { it.name }) { day ->
                        val isSelected = day == selectedDay
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDaySelected(day) },
                            label = {
                                Text(
                                    text = day.displayNamePt.uppercase(),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            } else null,
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Black.copy(alpha = 0.35f),
                                labelColor = Color.White.copy(alpha = 0.75f),
                                selectedContainerColor = Color(0xFFA855F7),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // ApiSourceFooter at bottom
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ApiSourceFooter(text = "Dados fornecidos por: Bangumi-Data & AniList API")
        }
    }
}

@Composable
fun FullTopHeroHeader(
    anime: Anime,
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    onAnimeClick: (Anime) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionState = rememberNotificationPermissionState()
    val prefs = remember { NotificationPreferences(context) }
    var isNotifEnabled by remember(anime.id) { mutableStateOf(prefs.isAnimeNotificationsEnabled(anime.id)) }
    var isFavorite by remember(anime.id) { mutableStateOf(prefs.isAnimeNotificationsEnabled(anime.id)) }

    // Parallax & Fade Effects
    val parallaxTranslationY = (scrollOffset * 0.35f)
    val alphaFraction = (1f - (scrollOffset / 600f)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .graphicsLayer {
                translationY = -parallaxTranslationY
                alpha = alphaFraction
            }
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onAnimeClick(anime) }
    ) {
        // 1. Featured Backdrop Image covering all the way to the top
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

        // 2. Dark Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // 3. Hero Bottom Info (Trending tag, Title, Broadcast, Watch Now button)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Trending:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )

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
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    MyApplicationAnim1Theme {
        ScheduleContent(
            selectedDay = ScheduleDay.MONDAY,
            uiState = ScheduleUiState.Loading,
            onDaySelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}
