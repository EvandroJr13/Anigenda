package agenda.de.myapplicationanim1.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 6.dp
                    ) {
                        BottomNavItem.entries.forEach { item ->
                            val selected = currentTab == item
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
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
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
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
                Crossfade(targetState = currentScreen, label = "TabTransition") { screen ->
                    when (screen) {
                        is AppScreen.Schedule -> {
                            ScheduleScreen(
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
