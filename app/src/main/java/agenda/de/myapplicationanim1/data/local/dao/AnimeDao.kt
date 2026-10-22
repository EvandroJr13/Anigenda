package agenda.de.myapplicationanim1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import agenda.de.myapplicationanim1.data.local.entity.AnimeEntity

@Dao
interface AnimeDao {
    @Query("SELECT * FROM animes_cache WHERE airingDay = :day")
    suspend fun getAnimesByDay(day: String): List<AnimeEntity>

    @Query("SELECT * FROM animes_cache ORDER BY score DESC")
    suspend fun getAllPopular(): List<AnimeEntity>

    @Query("SELECT * FROM animes_cache WHERE title LIKE '%' || :query || '%' OR titleEnglish LIKE '%' || :query || '%'")
    suspend fun searchAnimes(query: String): List<AnimeEntity>

    @Query("SELECT * FROM animes_cache WHERE id = :id")
    suspend fun getAnimeById(id: Int): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<AnimeEntity>)

    @Query("SELECT COUNT(*) FROM animes_cache")
    suspend fun getCount(): Int
}
