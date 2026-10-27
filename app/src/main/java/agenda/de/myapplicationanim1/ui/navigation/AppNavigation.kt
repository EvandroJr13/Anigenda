package agenda.de.myapplicationanim1.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
) {
    SCHEDULE("Calendário", Icons.Rounded.CalendarMonth),
    POPULAR("Populares", Icons.Rounded.AutoAwesome),
    SEARCH("Explorar", Icons.Rounded.Search),
    FAVORITES("Favoritos", Icons.Rounded.Favorite),
    SETTINGS("Ajustes", Icons.Rounded.Settings),
}

sealed interface AppScreen {
    data object Schedule : AppScreen
    data object Popular : AppScreen
    data object Search : AppScreen
    data object Favorites : AppScreen
    data object Settings : AppScreen
    data class Details(val animeId: Int) : AppScreen
}
