package agenda.de.myapplicationanim1.ui.popular

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.ui.components.AnimeCard
import agenda.de.myapplicationanim1.ui.components.ApiSourceFooter
import agenda.de.myapplicationanim1.ui.components.EmptyStateView
import agenda.de.myapplicationanim1.ui.components.ErrorStateView
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
import agenda.de.myapplicationanim1.ui.components.SectionHeader
import agenda.de.myapplicationanim1.ui.components.ShimmerGridLoading
import agenda.de.myapplicationanim1.ui.settings.AppThemeMode
import agenda.de.myapplicationanim1.ui.theme.AgendaDeAnimesTheme
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily

@Composable
fun PopularScreen(
    modifier: Modifier = Modifier,
    viewModel: PopularViewModel = viewModel(),
    onAnimeClick: (Anime) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PopularContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onRetry = { viewModel.refresh() },
        onAnimeClick = onAnimeClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularContent(
    uiState: PopularUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Atualizar",
                            tint = Color.White
                        )
                    }
                    GlobalNotificationAction()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = uiState is PopularUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ShimmerGridLoading()
                }

                AnimatedVisibility(
                    visible = uiState is PopularUiState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is PopularUiState.Error) {
                        ErrorStateView(
                            errorMessage = uiState.message,
                            onRetry = onRetry
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState is PopularUiState.Success,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is PopularUiState.Success) {
                        val animeList = uiState.animeList
                        if (animeList.isEmpty()) {
                            EmptyStateView(message = "Nenhum anime popular encontrado para este ano.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    SectionHeader(
                                        title = "Populares do Ano",
                                        subtitle = "Animes mais populares da temporada",
                                        itemCount = animeList.size,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    )
                                }

                                items(animeList, key = { it.id }) { anime ->
                                    AnimeCard(
                                        anime = anime,
                                        onClick = onAnimeClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                ApiSourceFooter(text = "Dados fornecidos por: AniList API")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PopularScreenPreview() {
    AgendaDeAnimesTheme(themeMode = AppThemeMode.BLACK) {
        PopularContent(
            uiState = PopularUiState.Loading,
            onRefresh = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}
