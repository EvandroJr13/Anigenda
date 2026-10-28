package agenda.de.myapplicationanim1.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import agenda.de.myapplicationanim1.ui.details.DetailsScreen
import agenda.de.myapplicationanim1.ui.favorites.FavoritesScreen
import agenda.de.myapplicationanim1.ui.navigation.AppScreen
import agenda.de.myapplicationanim1.ui.navigation.BottomNavItem
import agenda.de.myapplicationanim1.ui.popular.PopularScreen
import agenda.de.myapplicationanim1.ui.popular.PopularViewModel
import agenda.de.myapplicationanim1.ui.schedule.ScheduleScreen
import agenda.de.myapplicationanim1.ui.search.SearchScreen
import agenda.de.myapplicationanim1.ui.settings.SettingsScreen
import agenda.de.myapplicationanim1.ui.settings.SettingsViewModel
import agenda.de.myapplicationanim1.ui.theme.AgendaDeAnimesTheme

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    initialAnimeId: Int? = null
) {
    var hasShownFrontPage by remember { mutableStateOf(false) }
    val initialStack = remember(initialAnimeId) {
        if (initialAnimeId != null && initialAnimeId > 0) {
            listOf(AppScreen.Schedule, AppScreen.Details(initialAnimeId))
        } else {
            listOf(AppScreen.Schedule)
        }
    }
    val backStack = remember { mutableStateListOf<AppScreen>().apply { addAll(initialStack) } }
    val currentScreen = backStack.lastOrNull() ?: AppScreen.Schedule

    val currentTab = when (currentScreen) {
        is AppScreen.Schedule -> BottomNavItem.SCHEDULE
        is AppScreen.Popular -> BottomNavItem.POPULAR
        is AppScreen.Search -> BottomNavItem.SEARCH
        is AppScreen.Favorites -> BottomNavItem.FAVORITES
        is AppScreen.Settings -> BottomNavItem.SETTINGS
        is AppScreen.Details -> {
            val previousTabScreen = backStack.findLast { it !is AppScreen.Details }
            when (previousTabScreen) {
                is AppScreen.Popular -> BottomNavItem.POPULAR
                is AppScreen.Search -> BottomNavItem.SEARCH
                is AppScreen.Favorites -> BottomNavItem.FAVORITES
                is AppScreen.Settings -> BottomNavItem.SETTINGS
                else -> BottomNavItem.SCHEDULE
            }
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    AgendaDeAnimesTheme(
        themeMode = settingsViewModel.themeMode,
        backgroundColorHex = settingsViewModel.backgroundColorHex,
        systemThemeMode = settingsViewModel.systemThemeMode
    ) {
        Scaffold(
            bottomBar = {
                if (currentScreen !is AppScreen.Details) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 12.dp, end = 12.dp, bottom = 20.dp)
                            .height(84.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 1. Floating Navigation Bar Background Container
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .align(Alignment.BottomCenter)
                        ) {}

                        // 2. Floating Tabs Row (Selected tab rises up)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            BottomNavItem.entries.forEach { item ->
                                val selected = currentTab == item
                                val primaryColor = MaterialTheme.colorScheme.primary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(84.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (selected) {
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = BorderStroke(2.dp, primaryColor),
                                            shadowElevation = 14.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(68.dp)
                                                .padding(horizontal = 2.dp)
                                                .graphicsLayer {
                                                    translationY = -18f
                                                }
                                                .clickable {
                                                    val targetScreen = when (item) {
                                                        BottomNavItem.SCHEDULE -> AppScreen.Schedule
                                                        BottomNavItem.POPULAR -> AppScreen.Popular
                                                        BottomNavItem.SEARCH -> AppScreen.Search
                                                        BottomNavItem.FAVORITES -> AppScreen.Favorites
                                                        BottomNavItem.SETTINGS -> AppScreen.Settings
                                                    }
                                                    if (currentScreen != targetScreen) {
                                                        backStack.clear()
                                                        backStack.add(targetScreen)
                                                    }
                                                }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.title,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = primaryColor,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(68.dp)
                                                .clickable {
                                                    val targetScreen = when (item) {
                                                        BottomNavItem.SCHEDULE -> AppScreen.Schedule
                                                        BottomNavItem.POPULAR -> AppScreen.Popular
                                                        BottomNavItem.SEARCH -> AppScreen.Search
                                                        BottomNavItem.FAVORITES -> AppScreen.Favorites
                                                        BottomNavItem.SETTINGS -> AppScreen.Settings
                                                    }
                                                    if (currentScreen != targetScreen) {
                                                        backStack.clear()
                                                        backStack.add(targetScreen)
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = item.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (currentScreen is AppScreen.Details) PaddingValues(0.dp) else innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 3 }
                        ) togetherWith fadeOut() + slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        )
                    },
                    label = "TabSlideTransition"
                ) { screen ->
                    when (screen) {
                        is AppScreen.Schedule -> {
                            ScheduleScreen(
                                showFrontPageOnLaunch = !hasShownFrontPage,
                                onFrontPageShown = { hasShownFrontPage = true },
                                onAnimeClick = { anime ->
                                    backStack.add(AppScreen.Details(anime.id))
                                }
                            )
                        }
                        is AppScreen.Popular -> {
                            val popularViewModel: PopularViewModel = viewModel()
                            PopularScreen(
                                viewModel = popularViewModel,
                                onAnimeClick = { anime ->
                                    backStack.add(AppScreen.Details(anime.id))
                                }
                            )
                        }
                        is AppScreen.Search -> {
                            SearchScreen(
                                onAnimeClick = { anime ->
                                    backStack.add(AppScreen.Details(anime.id))
                                }
                            )
                        }
                        is AppScreen.Favorites -> {
                            FavoritesScreen(
                                onAnimeClick = { animeId ->
                                    backStack.add(AppScreen.Details(animeId))
                                }
                            )
                        }
                        is AppScreen.Settings -> {
                            SettingsScreen(
                                currentTheme = settingsViewModel.themeMode,
                                onThemeSelected = { newMode ->
                                    settingsViewModel.updateThemeMode(newMode)
                                },
                                viewModel = settingsViewModel,
                                onAnimeClick = { animeId ->
                                    backStack.add(AppScreen.Details(animeId))
                                }
                            )
                        }
                        is AppScreen.Details -> {
                            DetailsScreen(
                                animeId = screen.animeId,
                                onBackClick = {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    } else {
                                        backStack.clear()
                                        backStack.add(AppScreen.Schedule)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
