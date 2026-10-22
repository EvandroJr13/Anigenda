package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeRepositoryTest {

    @Test
    fun getSchedules_returnsLocalCacheAnimes() = runTest {
        val repository = AnimeRepositoryImpl()
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertFalse(successRes.data.isEmpty())
    }
}
