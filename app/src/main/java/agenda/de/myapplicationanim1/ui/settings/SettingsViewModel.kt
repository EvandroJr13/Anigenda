package agenda.de.myapplicationanim1.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.NotificationPreferences
import agenda.de.myapplicationanim1.data.util.NotifiedAnime
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.data.util.SystemThemeMode
import agenda.de.myapplicationanim1.data.util.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppThemeMode(val title: String, val defaultBgHex: String) {
    SYSTEM("Acompanhar Sistema", "#0B0E14"),
    LIGHT("Claro", "#FFFFFF"),
    DARK("Escuro (Padrão)", "#121212"),
    BLACK("Black & Neon Blue", "#0B0E14"),
    PURE_BLACK("Preto OLED", "#000000"),
    DARK_BLUE("Azul Escuro", "#0A1128"),
    DARK_GRAY("Cinza Escuro", "#121212"),
    DARK_PURPLE("Roxo Escuro", "#120A2A"),
    DARK_RED("Vermelho Escuro", "#1A0A0A"),
    GREEN_BLACK("Verde com Preto", "#000000"),
    ORANGE_BLACK("Laranja com Preto", "#000000"),
    PINK_BLACK("Rosa com Preto", "#000000")
}

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AnimeRepository = AnimeRepositoryImpl()
) : AndroidViewModel(application) {

    private val themePrefs = ThemePreferences(application)
    private val notifPrefs = NotificationPreferences(application)

    var themeMode by mutableStateOf(themePrefs.appThemeMode)
        private set

    var systemThemeMode by mutableStateOf(themePrefs.systemThemeMode)
        private set

    var backgroundColorHex by mutableStateOf(themePrefs.backgroundColorHex)
        private set

    var isGlobalNotificationsEnabled by mutableStateOf(notifPrefs.isGlobalNotificationsEnabled)
        private set

    private val _notifiedAnimes = MutableStateFlow<List<NotifiedAnime>>(emptyList())
    val notifiedAnimes: StateFlow<List<NotifiedAnime>> = _notifiedAnimes.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        themeMode = themePrefs.appThemeMode
        systemThemeMode = themePrefs.systemThemeMode
        backgroundColorHex = themePrefs.backgroundColorHex
        isGlobalNotificationsEnabled = notifPrefs.isGlobalNotificationsEnabled
        loadNotifiedAnimes()
    }

    fun updateThemeMode(mode: AppThemeMode) {
        themeMode = mode
        themePrefs.appThemeMode = mode
        if (mode == AppThemeMode.LIGHT) {
            systemThemeMode = SystemThemeMode.LIGHT
            themePrefs.systemThemeMode = SystemThemeMode.LIGHT
        } else if (mode == AppThemeMode.SYSTEM) {
            systemThemeMode = SystemThemeMode.SYSTEM
            themePrefs.systemThemeMode = SystemThemeMode.SYSTEM
        } else {
            systemThemeMode = SystemThemeMode.DARK
            themePrefs.systemThemeMode = SystemThemeMode.DARK
        }
        backgroundColorHex = mode.defaultBgHex
        themePrefs.backgroundColorHex = mode.defaultBgHex
    }

    fun updateSystemThemeMode(mode: SystemThemeMode) {
        systemThemeMode = mode
        themePrefs.systemThemeMode = mode
        when (mode) {
            SystemThemeMode.LIGHT -> {
                themeMode = AppThemeMode.LIGHT
                themePrefs.appThemeMode = AppThemeMode.LIGHT
                backgroundColorHex = "#FFFFFF"
                themePrefs.backgroundColorHex = "#FFFFFF"
            }
            SystemThemeMode.DARK -> {
                if (themeMode == AppThemeMode.LIGHT || themeMode == AppThemeMode.SYSTEM) {
                    themeMode = AppThemeMode.BLACK
                    themePrefs.appThemeMode = AppThemeMode.BLACK
                    backgroundColorHex = "#0B0E14"
                    themePrefs.backgroundColorHex = "#0B0E14"
                }
            }
            SystemThemeMode.SYSTEM -> {
                themeMode = AppThemeMode.SYSTEM
                themePrefs.appThemeMode = AppThemeMode.SYSTEM
            }
        }
    }

    fun updateBackgroundColor(hex: String) {
        backgroundColorHex = hex
        themePrefs.backgroundColorHex = hex
    }

    fun toggleGlobalNotifications(enabled: Boolean) {
        isGlobalNotificationsEnabled = enabled
        notifPrefs.isGlobalNotificationsEnabled = enabled
        agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler.rescheduleAllAlarms(getApplication())
    }

    fun loadNotifiedAnimes() {
        val list = notifPrefs.getNotifiedAnimes()
        _notifiedAnimes.value = list

        // Fetch details for items missing titles/images
        list.filter { it.title.startsWith("Anime #") || it.imageUrl.isBlank() }.forEach { notified ->
            viewModelScope.launch {
                repository.getAnimeDetails(notified.id).collect { resource ->
                    if (resource is Resource.Success) {
                        val anime = resource.data
                        notifPrefs.setAnimeNotificationsEnabled(anime, enabled = true)
                        agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler.scheduleAlarm(getApplication(), anime)
                        _notifiedAnimes.value = notifPrefs.getNotifiedAnimes()
                    }
                }
            }
        }
    }

    fun removeNotifiedAnime(animeId: Int) {
        notifPrefs.setAnimeNotificationsEnabled(animeId = animeId, enabled = false)
        agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler.cancelAlarm(getApplication(), animeId)
        _notifiedAnimes.value = notifPrefs.getNotifiedAnimes()
    }
}
