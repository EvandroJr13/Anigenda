package agenda.de.myapplicationanim1.ui.search

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
import agenda.de.myapplicationanim1.ui.components.ShimmerGridLoading
import agenda.de.myapplicationanim1.ui.theme.MyApplicationAnim1Theme

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    onAnimeClick: (Anime) -> Unit = {},
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        query = query,
        selectedGenre = selectedGenre,
        selectedStatus = selectedStatus,
        availableGenres = viewModel.availableGenres,
        availableStatuses = viewModel.availableStatuses,
        uiState = uiState,
        onQueryChanged = { viewModel.onQueryChanged(it) },
        onSearchSubmitted = { viewModel.onSearchClicked() },
        onClearQuery = { viewModel.clearQuery() },
        onToggleGenre = { viewModel.toggleGenre(it) },
        onToggleStatus = { viewModel.toggleStatus(it) },
        onRetry = { viewModel.retry() },
        onAnimeClick = onAnimeClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    query: String,
    selectedGenre: String?,
    selectedStatus: String?,
    availableGenres: List<String>,
    availableStatuses: List<String>,
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearchSubmitted: () -> Unit,
    onClearQuery: () -> Unit,
    onToggleGenre: (String) -> Unit,
    onToggleStatus: (String) -> Unit,
    onRetry: () -> Unit,
    onAnimeClick: (Anime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showGenreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val blackShadow = Shadow(color = Color.Black, offset = Offset(0f, 0f), blurRadius = 6f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ani",
                            style = TextStyle(
                                fontFamily = ChewyFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                shadow = blackShadow
                            )
                        )
                        Text(
                            text = "genda",
                            style = TextStyle(
                                fontFamily = ChewyFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                shadow = blackShadow
                            )
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
            // Search OutlinedTextField Header
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Digite o nome do anime...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Ícone de busca",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = onClearQuery) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Limpar busca"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                onSearchSubmitted()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Filter Chips (Genres & Statuses)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Status Filter Chips
                        items(availableStatuses, key = { "status_$it" }) { status ->
                            val isSelected = selectedStatus == status
                            val displayName = when (status) {
                                "Airing" -> "Em Exibição"
                                "Finished" -> "Concluídos"
                                "Upcoming" -> "Previsões / Futuros"
                                else -> status
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleStatus(status) },
                                label = {
                                    Text(
                                        text = displayName,
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
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        // Genre Selection Button
                        item {
                            val isGenreSelected = selectedGenre != null
                            val genreButtonText = if (selectedGenre != null) {
                                val displayNamePt = when (selectedGenre) {
                                    "Action" -> "Ação"
                                    "Adventure" -> "Aventura"
                                    "Comedy" -> "Comédia"
                                    "Drama" -> "Drama"
                                    "Fantasy" -> "Fantasia"
                                    "Romance" -> "Romance"
                                    "Sci-Fi" -> "Ficção Científica"
                                    "Supernatural" -> "Sobrenatural"
                                    else -> selectedGenre
                                }
                                "Tag: $displayNamePt"
                            } else {
                                "Filtrar por Tags..."
                            }

                            OutlinedButton(
                                onClick = { showGenreDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isGenreSelected) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
                                    contentColor = if (isGenreSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (isGenreSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = genreButtonText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isGenreSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Genre Selection Dialog Box
                    if (showGenreDialog) {
                        AlertDialog(
                            onDismissRequest = { showGenreDialog = false },
                            title = {
                                Text(text = "Selecionar Tag / Gênero", fontWeight = FontWeight.Bold)
                            },
                            text = {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.heightIn(max = 350.dp)
                                ) {
                                    item {
                                        TextButton(
                                            onClick = {
                                                if (selectedGenre != null) {
                                                    onToggleGenre(selectedGenre)
                                                }
                                                showGenreDialog = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("✨ Todas as Tags (Limpar Filtro)")
                                        }
                                    }
                                    items(availableGenres) { genre ->
                                        val isSelected = selectedGenre == genre
                                        val displayNamePt = when (genre) {
                                            "Action" -> "Ação"
                                            "Adventure" -> "Aventura"
                                            "Comedy" -> "Comédia"
                                            "Drama" -> "Drama"
                                            "Fantasy" -> "Fantasia"
                                            "Romance" -> "Romance"
                                            "Sci-Fi" -> "Ficção Científica"
                                            "Supernatural" -> "Sobrenatural"
                                            else -> genre
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onToggleGenre(genre)
                                                    showGenreDialog = false
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = displayNamePt,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showGenreDialog = false }) {
                                    Text("Fechar")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedVisibility(
                    visible = uiState is SearchUiState.Idle,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Pesquise por animes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Digite o título de um anime ou selecione um filtro acima para começar.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState is SearchUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ShimmerGridLoading()
                }

                AnimatedVisibility(
                    visible = uiState is SearchUiState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is SearchUiState.Error) {
                        ErrorStateView(
                            errorMessage = uiState.message,
                            onRetry = onRetry
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState is SearchUiState.Success,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is SearchUiState.Success) {
                        val animeList = uiState.animeList
                        if (animeList.isEmpty()) {
                            EmptyStateView(
                                message = if (query.isNotEmpty()) {
                                    "Nenhum anime encontrado para \"$query\"."
                                } else {
                                    "Nenhum anime encontrado com os filtros selecionados."
                                }
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(animeList, key = { it.id }) { anime ->
                                    val ep = anime.nextAiringEpisode?.episode ?: if (anime.episodes > 0) anime.episodes else 1
                                    val seasonBadgeText = when (selectedStatus) {
                                        "Upcoming" -> "Próxima: Temp. ${anime.totalSeasons}"
                                        "Finished" -> "${anime.totalSeasons} Temporada${if (anime.totalSeasons > 1) "s" else ""}"
                                        else -> "Ep $ep, Temporadas ${anime.totalSeasons}"
                                    }
                                    AnimeCard(
                                        anime = anime,
                                        showNotificationBell = false,
                                        episodeBadgeText = seasonBadgeText,
                                        onClick = onAnimeClick
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ApiSourceFooter(text = "Dados fornecidos por: AniList API")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    MyApplicationAnim1Theme {
        SearchContent(
            query = "",
            selectedGenre = null,
            selectedStatus = null,
            availableGenres = listOf("Action", "Adventure", "Comedy"),
            availableStatuses = listOf("Airing", "Finished"),
            uiState = SearchUiState.Idle,
            onQueryChanged = {},
            onSearchSubmitted = {},
            onClearQuery = {},
            onToggleGenre = {},
            onToggleStatus = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}
