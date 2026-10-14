package agenda.de.myapplicationanim1.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agenda.de.myapplicationanim1.R
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.ui.components.AnimeCard
import agenda.de.myapplicationanim1.ui.components.ApiSourceFooter
import agenda.de.myapplicationanim1.ui.components.EmptyStateView
import agenda.de.myapplicationanim1.ui.components.ErrorStateView
import agenda.de.myapplicationanim1.ui.components.FrontPageDialog
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
import agenda.de.myapplicationanim1.ui.components.HeroBanner
import agenda.de.myapplicationanim1.ui.components.SectionHeader
import agenda.de.myapplicationanim1.ui.components.ShimmerGridLoading
import agenda.de.myapplicationanim1.ui.theme.MyApplicationAnim1Theme
import androidx.compose.foundation.layout.offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = viewModel(),
    onAnimeClick: (Anime) -> Unit = {},
) {
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScheduleContent(
        selectedDay = selectedDay,
        uiState = uiState,
        onDaySelected = { viewModel.selectDay(it) },
        onRetry = { viewModel.retry() },
        onAnimeClick = onAnimeClick,
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
    modifier: Modifier = Modifier,
) {
    var showFrontPage by rememberSaveable { mutableStateOf(true) }

    // Front Page / Destaque do Dia Dialog on launch
    if (showFrontPage && uiState is ScheduleUiState.Success) {
        val animeList = uiState.animeList
        if (animeList.isNotEmpty()) {
            val featuredAnime = animeList.maxByOrNull { it.score } ?: animeList.firstOrNull()
            if (featuredAnime != null) {
                FrontPageDialog(
                    anime = featuredAnime,
                    onDismiss = { showFrontPage = false },
                    onViewDetails = { anime ->
                        showFrontPage = false
                        onAnimeClick(anime)
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.clickable {
                            if (uiState is ScheduleUiState.Success) {
                                showFrontPage = true
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "AG Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "Animes do dia",
                            fontFamily = ChewyFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    GlobalNotificationAction()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Day Filter Chips Row
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ScheduleDay.entries.toTypedArray(), key = { it.name }) { day ->
                        val isSelected = day == selectedDay
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDaySelected(day) },
                            label = {
                                Text(
                                    text = day.displayNamePt,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedVisibility(
                    visible = uiState is ScheduleUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ShimmerGridLoading()
                }

                AnimatedVisibility(
                    visible = uiState is ScheduleUiState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is ScheduleUiState.Error) {
                        ErrorStateView(
                            errorMessage = uiState.message,
                            onRetry = onRetry
                        )
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
                            EmptyStateView(message = "Nenhum anime programado para ${selectedDay.displayNamePt}.")
                        } else {
                            val featuredAnime = animeList.maxByOrNull { it.score } ?: animeList.firstOrNull()
                            val sectionTitle = if (selectedDay == ScheduleDay.today()) "Seleção de Hoje" else "Seleção do Dia"

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // 1. Collapsing Featured Hero Banner
                                if (featuredAnime != null) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        HeroBanner(
                                            anime = featuredAnime,
                                            onAnimeClick = onAnimeClick,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                // 2. Section Header ("Seleção de Hoje" / "Seleção do Dia")
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    SectionHeader(
                                        title = sectionTitle,
                                        subtitle = "Programação de ${selectedDay.displayNamePt}",
                                        itemCount = animeList.size,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                // 3. Anime Schedule Grid Cards
                                items(animeList, key = { it.id }) { anime ->
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
            ApiSourceFooter(text = "Dados fornecidos por: Bangumi-Data & AniList API")

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
