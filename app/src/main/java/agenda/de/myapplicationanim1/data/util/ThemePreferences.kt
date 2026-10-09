package agenda.de.myapplicationanim1.data.util

import android.content.Context
import android.content.SharedPreferences
import agenda.de.myapplicationanim1.ui.settings.AppThemeMode

enum class SystemThemeMode(val title: String) {
    SYSTEM("Acompanhar Sistema"),
    DARK("Escuro"),
    LIGHT("Claro")
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_preferences", Context.MODE_PRIVATE)

    var systemThemeMode: SystemThemeMode
        get() {
            val name = prefs.getString(KEY_SYSTEM_THEME_MODE, SystemThemeMode.DARK.name) ?: SystemThemeMode.DARK.name
            return try { SystemThemeMode.valueOf(name) } catch (_: Exception) { SystemThemeMode.DARK }
        }
        set(value) = prefs.edit().putString(KEY_SYSTEM_THEME_MODE, value.name).apply()

    var appThemeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_APP_THEME_MODE, AppThemeMode.BLACK.name) ?: AppThemeMode.BLACK.name
            return try { AppThemeMode.valueOf(name) } catch (_: Exception) { AppThemeMode.BLACK }
        }
        set(value) = prefs.edit().putString(KEY_APP_THEME_MODE, value.name).apply()

    var backgroundColorHex: String
        get() = prefs.getString(KEY_BACKGROUND_COLOR_HEX, "#0B0E14") ?: "#0B0E14"
        set(value) = prefs.edit().putString(KEY_BACKGROUND_COLOR_HEX, value).apply()

    companion object {
        private const val KEY_SYSTEM_THEME_MODE = "key_system_theme_mode"
        private const val KEY_APP_THEME_MODE = "key_app_theme_mode"
        private const val KEY_BACKGROUND_COLOR_HEX = "key_background_color_hex"
    }
}
