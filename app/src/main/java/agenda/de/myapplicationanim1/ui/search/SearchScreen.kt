package agenda.de.myapplicationanim1.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                            color = Color(0xFFA855F7)
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

                        // Genre Filter Chips
                        items(availableGenres, key = { "genre_$it" }) { genre ->
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
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleGenre(genre) },
                                label = {
                                    Text(
                                        text = displayNamePt,
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
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
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
