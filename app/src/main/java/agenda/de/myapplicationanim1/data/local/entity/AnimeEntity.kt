package agenda.de.myapplicationanim1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.model.ExternalLinkInfo

@Entity(tableName = "animes_cache")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val imageUrl: String,
    val synopsis: String,
    val score: Double,
    val episodes: Int,
    val status: String,
    val airingDay: String,
    val genresString: String,
    val lastUpdated: Long
)

fun AnimeEntity.toDomain(): Anime {
    return Anime(
        id = id,
        title = title,
        titleRomaji = titleRomaji,
        titleEnglish = titleEnglish,
        imageUrl = imageUrl,
        largeImageUrl = imageUrl,
        synopsis = synopsis,
        score = score,
        episodes = episodes,
        status = status,
        aired = "",
        broadcast = BroadcastInfo(day = airingDay, time = "", timezone = "UTC", stringFormat = airingDay),
        genres = if (genresString.isNotBlank()) genresString.split(",") else emptyList(),
        airingDay = airingDay,
        externalLinks = emptyList()
    )
}

fun Anime.toEntity(): AnimeEntity {
    return AnimeEntity(
        id = id,
        title = title,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji,
        imageUrl = imageUrl,
        synopsis = synopsis,
        score = score,
        episodes = episodes,
        status = status,
        airingDay = airingDay,
        genresString = genres.joinToString(","),
        lastUpdated = System.currentTimeMillis()
    )
}
