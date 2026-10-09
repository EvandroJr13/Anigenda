package agenda.de.myapplicationanim1

import android.app.Application
import android.content.SharedPreferences
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.util.NotificationPreferences
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.data.util.SystemThemeMode
import agenda.de.myapplicationanim1.data.util.ThemePreferences
import agenda.de.myapplicationanim1.ui.settings.AppThemeMode
import agenda.de.myapplicationanim1.ui.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: FakeApplication
    private lateinit var themePrefs: ThemePreferences
    private lateinit var notificationPrefs: NotificationPreferences

    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values

        override fun getString(key: String?, defValue: String?): String? =
            values[key] as? String ?: defValue

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            (values[key] as? Set<String>)?.toMutableSet() ?: defValues

        override fun getInt(key: String?, defValue: Int): Int =
            values[key] as? Int ?: defValue

        override fun getLong(key: String?, defValue: Long): Long =
            values[key] as? Long ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float =
            values[key] as? Float ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            values[key] as? Boolean ?: defValue

        override fun contains(key: String?): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = FakeEditor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        inner class FakeEditor : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun putStringSet(key: String?, valuesSet: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) values[key] = valuesSet?.toSet()
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) values[key] = value
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) values.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                values.clear()
                return this
            }

            override fun commit(): Boolean = true
            override fun apply() {}
        }
    }

    private class FakeApplication : Application() {
        private val prefsMap = mutableMapOf<String, SharedPreferences>()

        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
            return prefsMap.getOrPut(name ?: "default") { FakeSharedPreferences() }
        }

        override fun getApplicationContext(): Application = this
    }

    private class FakeAnimeRepository : AnimeRepository {
        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flowOf(Resource.Error("Not found"))
    }

    @Before
    fun setUp() {
        context = FakeApplication()
        themePrefs = ThemePreferences(context)
        notificationPrefs = NotificationPreferences(context)
    }

    @Test
    fun updateThemeMode_updatesStateAndPersistsInSharedPreferences() = runTest {
        val viewModel = SettingsViewModel(context, FakeAnimeRepository())

        viewModel.updateThemeMode(AppThemeMode.PURE_BLACK)

        assertEquals(AppThemeMode.PURE_BLACK, viewModel.themeMode)
        assertEquals("#000000", viewModel.backgroundColorHex)
        assertEquals(AppThemeMode.PURE_BLACK, themePrefs.appThemeMode)
        assertEquals("#000000", themePrefs.backgroundColorHex)
    }

    @Test
    fun updateSystemThemeMode_updatesStateAndPersistsInSharedPreferences() = runTest {
        val viewModel = SettingsViewModel(context, FakeAnimeRepository())

        viewModel.updateSystemThemeMode(SystemThemeMode.LIGHT)

        assertEquals(SystemThemeMode.LIGHT, viewModel.systemThemeMode)
        assertEquals(AppThemeMode.LIGHT, viewModel.themeMode)
        assertEquals("#FFFFFF", viewModel.backgroundColorHex)
        assertEquals(SystemThemeMode.LIGHT, themePrefs.systemThemeMode)
        assertEquals("#FFFFFF", themePrefs.backgroundColorHex)
    }

    @Test
    fun updateBackgroundColor_updatesStateAndPersists() = runTest {
        val viewModel = SettingsViewModel(context, FakeAnimeRepository())

        viewModel.updateBackgroundColor("#0A1128")

        assertEquals("#0A1128", viewModel.backgroundColorHex)
        assertEquals("#0A1128", themePrefs.backgroundColorHex)
    }

    @Test
    fun toggleGlobalNotifications_updatesStateAndPersists() = runTest {
        val viewModel = SettingsViewModel(context, FakeAnimeRepository())

        viewModel.toggleGlobalNotifications(false)

        assertFalse(viewModel.isGlobalNotificationsEnabled)
        assertFalse(notificationPrefs.isGlobalNotificationsEnabled)
    }

    @Test
    fun addAndRemoveNotifiedAnime_updatesListAndPreferences() = runTest {
        val viewModel = SettingsViewModel(context, FakeAnimeRepository())

        notificationPrefs.setAnimeNotificationsEnabled(animeId = 101, title = "Attack on Titan", imageUrl = "http://example.com/aot.jpg", enabled = true)
        viewModel.loadNotifiedAnimes()

        val list = viewModel.notifiedAnimes.value
        assertEquals(1, list.size)
        assertEquals(101, list[0].id)
        assertEquals("Attack on Titan", list[0].title)

        viewModel.removeNotifiedAnime(101)

        assertTrue(viewModel.notifiedAnimes.value.isEmpty())
        assertFalse(notificationPrefs.isAnimeNotificationsEnabled(101))
    }
}
