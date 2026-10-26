package agenda.de.myapplicationanim1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import agenda.de.myapplicationanim1.data.util.SystemThemeMode
import agenda.de.myapplicationanim1.ui.settings.AppThemeMode

private val BlackNeonBlueColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3B0764),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = Color(0xFFC084FC),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFEAB308),
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF131722),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A202C),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = Color(0xFF131722),
    surfaceContainerHigh = Color(0xFF1A202C),
    surfaceContainerHighest = Color(0xFF242C3D),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val PureBlackColorScheme = darkColorScheme(
    primary = Color(0xFF00A8FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF003B6F),
    onPrimaryContainer = Color(0xFFD0E8FF),
    secondary = Color(0xFF00A8FF),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF38BDF8),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFF8A99AD),
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF141414),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val DarkBlueColorScheme = darkColorScheme(
    primary = Color(0xFF3A86EF),
    secondary = Color(0xFF4CC9F0),
    tertiary = Color(0xFF7209B7),
    background = Color(0xFF0A1128),
    surface = Color(0xFF001F54),
    surfaceContainer = Color(0xFF001F54),
    surfaceContainerHigh = Color(0xFF1C2541)
)

private val DarkGrayColorScheme = darkColorScheme(
    primary = Color(0xFF90E0EF),
    secondary = Color(0xFF00B4D8),
    tertiary = Color(0xFF0077B6),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF2D2D2D)
)

private val DarkPurpleColorScheme = darkColorScheme(
    primary = Color(0xFF9D4EDD),
    secondary = Color(0xFFC77DFF),
    tertiary = Color(0xFFE0AAFF),
    background = Color(0xFF120A2A),
    surface = Color(0xFF1D113B),
    surfaceContainer = Color(0xFF1D113B),
    surfaceContainerHigh = Color(0xFF2A1A5E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF6B5778),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainer = Color(0xFFEEF0F8),
    surfaceContainerHigh = Color(0xFFE8EAFA)
)

fun parseHexColor(hex: String, fallback: Color): Color {
    if (hex.isBlank()) return fallback
    return try {
        val cleanHex = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun AgendaDeAnimesTheme(
    themeMode: AppThemeMode = AppThemeMode.BLACK,
    backgroundColorHex: String? = null,
    systemThemeMode: SystemThemeMode = SystemThemeMode.DARK,
    content: @Composable () -> Unit
) {
    MyApplicationAnim1Theme(
        themeMode = themeMode,
        backgroundColorHex = backgroundColorHex,
        systemThemeMode = systemThemeMode,
        content = content
    )
}

@Composable
fun MyApplicationAnim1Theme(
    themeMode: AppThemeMode = AppThemeMode.BLACK,
    backgroundColorHex: String? = null,
    systemThemeMode: SystemThemeMode = SystemThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when {
        themeMode == AppThemeMode.LIGHT -> false
        themeMode == AppThemeMode.SYSTEM -> isSystemDark
        systemThemeMode == SystemThemeMode.LIGHT -> false
        systemThemeMode == SystemThemeMode.SYSTEM -> isSystemDark
        else -> true
    }

    var baseScheme = if (isDark) {
        when (themeMode) {
            AppThemeMode.BLACK -> BlackNeonBlueColorScheme
            AppThemeMode.PURE_BLACK -> PureBlackColorScheme
            AppThemeMode.DARK_BLUE -> DarkBlueColorScheme
            AppThemeMode.DARK_GRAY -> DarkGrayColorScheme
            AppThemeMode.DARK_PURPLE -> DarkPurpleColorScheme
            AppThemeMode.DARK -> DarkGrayColorScheme
            else -> BlackNeonBlueColorScheme
        }
    } else {
        LightColorScheme
    }

    if (!backgroundColorHex.isNullOrBlank()) {
        val customColor = parseHexColor(backgroundColorHex, baseScheme.background)
        baseScheme = baseScheme.copy(background = customColor)
    }

    MaterialTheme(
        colorScheme = baseScheme,
        typography = Typography,
        content = content
    )
}
