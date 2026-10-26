package agenda.de.myapplicationanim1.ui.favorites

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agenda.de.myapplicationanim1.R
import agenda.de.myapplicationanim1.data.util.FavoriteAnime
import agenda.de.myapplicationanim1.data.util.FavoriteEpisode
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favoriteAnimes by viewModel.favoriteAnimes.collectAsStateWithLifecycle()
    val favoriteEpisodes by viewModel.favoriteEpisodes.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

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
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "❤️ Animes (${favoriteAnimes.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "🎬 Episódios (${favoriteEpisodes.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (selectedTabIndex == 0) {
                    if (favoriteAnimes.isEmpty()) {
                        EmptyFavoritesState(
                            title = "Nenhum anime amado",
                            subtitle = "Toque no ícone de coração nos animes para adicioná-los aqui."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoriteAnimes, key = { it.id }) { anime ->
                                FavoriteAnimeCard(
                                    anime = anime,
                                    onAnimeClick = { onAnimeClick(anime.id) },
                                    onRemoveClick = { viewModel.removeFavoriteAnime(anime.id) }
                                )
                            }
                        }
                    }
                } else {
                    if (favoriteEpisodes.isEmpty()) {
                        EmptyFavoritesState(
                            title = "Nenhum episódio amado",
                            subtitle = "Abra a página de detalhes de um anime e toque no coração do episódio para salvá-lo."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoriteEpisodes, key = { it.id }) { episode ->
                                FavoriteEpisodeRow(
                                    episode = episode,
                                    onAnimeClick = { onAnimeClick(episode.animeId) },
                                    onRemoveClick = {
                                        viewModel.removeFavoriteEpisode(
                                            episode.animeId,
                                            episode.episodeNumber
                                        )
                                    },
                                    onCommentChange = { newComment ->
                                        viewModel.updateEpisodeComment(
                                            episode.animeId,
                                            episode.episodeNumber,
                                            newComment
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteAnimeCard(
    anime: FavoriteAnime,
    onAnimeClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAnimeClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Remover dos favoritos",
                        tint = Color(0xFFFF4081)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (anime.score > 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", anime.score),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteEpisodeRow(
    episode: FavoriteEpisode,
    onAnimeClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onCommentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var commentText by remember(episode.comment) { mutableStateOf(episode.comment) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp, 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onAnimeClick() }
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(episode.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = episode.animeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAnimeClick() }
                ) {
                    Text(
                        text = episode.animeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = episode.episodeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Remover episódio dos favoritos",
                        tint = Color(0xFFFF4081)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment Section
            if (isEditing) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Seu comentário sobre o episódio...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        isEditing = false
                        onCommentChange(commentText)
                    }) {
                        Text("Salvar")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { isEditing = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (episode.comment.isNotBlank()) episode.comment else "Toque para adicionar um comentário...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (episode.comment.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = if (episode.comment.isNotBlank()) FontStyle.Normal else FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
