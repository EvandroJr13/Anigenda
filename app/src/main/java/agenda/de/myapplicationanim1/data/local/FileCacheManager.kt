package agenda.de.myapplicationanim1.data.local

import android.content.Context
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.scraper.MalSeasonalScraper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class FileCacheManager(private val context: Context?) {
    private val gson = Gson()
    private val scraper = MalSeasonalScraper()

    private fun getCacheFile(): File? {
        if (context == null) return null
        return File(context.cacheDir, "animes_cache_file.json")
    }

    suspend fun getCachedAnimes(): List<Anime> {
        val file = getCacheFile()
        if (file != null && file.exists()) {
            try {
                val json = file.readText()
                val type = object : TypeToken<List<Anime>>() {}.type
                val list = gson.fromJson<List<Anime>>(json, type)
                if (!list.isNullOrEmpty()) return list
            } catch (_: Exception) {}
        }

        // Se o arquivo de cache não existir ou estiver vazio, busca da web via Web Scraping e salva no arquivo
        try {
            val seasonal = scraper.scrapeSeasonalAnimes()
            val list = seasonal.map { item ->
                Anime(
                    id = item.id.toIntOrNull() ?: item.id.hashCode(),
                    title = item.name,
                    titleRomaji = item.name,
                    titleEnglish = item.name,
                    imageUrl = item.coverUrl,
                    largeImageUrl = item.coverUrl,
                    synopsis = "Studio: ${item.studio} | Lançamento: ${item.releaseDate}",
                    score = 8.5,
                    episodes = 12,
                    status = "RELEASING",
                    aired = item.releaseDate,
                    airingDay = "Segunda",
                    genres = item.tags,
                    externalLinks = emptyList()
                )
            }
            saveCache(list)
            return list
        } catch (e: Exception) {
            return emptyList()
        }
    }

    fun saveCache(animes: List<Anime>) {
        val file = getCacheFile() ?: return
        try {
            val json = gson.toJson(animes)
            file.writeText(json)
        } catch (_: Exception) {}
    }
}
