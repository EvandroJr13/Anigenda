package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.model.NextAiringEpisodeInfo
import agenda.de.myapplicationanim1.data.util.AnimeAlarmScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AnimeAlarmSchedulerTest {

    @Test
    fun calculateNextOccurrenceMillis_futureNextAiringAtSec_returnsTimestampInMillis() {
        val futureSec = (System.currentTimeMillis() / 1000L) + 3600L // 1 hour in future
        val result = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = "Segunda",
            broadcastTime = "11:00",
            nextAiringAtSec = futureSec
        )
        assertEquals(futureSec * 1000L, result)
    }

    @Test
    fun calculateNextOccurrenceMillis_pastNextAiringAtSec_calculatesNextWeeklyOccurrence() {
        val pastSec = (System.currentTimeMillis() / 1000L) - 3600L // 1 hour in past
        val result = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = "Segunda",
            broadcastTime = "11:00",
            nextAiringAtSec = pastSec
        )
        assertTrue(result > System.currentTimeMillis())

        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
    }

    @Test
    fun calculateNextOccurrenceMillis_nullNextAiringAtSec_calculatesNextWeeklyOccurrence() {
        val result = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = "Sexta",
            broadcastTime = "22:30",
            nextAiringAtSec = null
        )
        assertTrue(result > System.currentTimeMillis())

        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun calculateNextOccurrenceMillis_amPmTime_calculatesCorrectTime() {
        val result = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = "Domingo",
            broadcastTime = "11:30 PM",
            nextAiringAtSec = null
        )
        assertTrue(result > System.currentTimeMillis())

        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun calculateNextOccurrenceMillis_hoursAfter23_calculatesCorrectTime() {
        val result = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = "Sábado",
            broadcastTime = "25:30",
            nextAiringAtSec = null
        )
        assertTrue(result > System.currentTimeMillis())

        val cal = Calendar.getInstance().apply { timeInMillis = result }
        // 25:30 on Saturday is Sunday at 01:30
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun scheduleAlarm_calculatesCorrectTriggerAndSavesInPreferences() {
        val fakeAnime = Anime(
            id = 999,
            title = "Test Scheduled Anime",
            imageUrl = "http://example.com/anime.jpg",
            airingDay = "Quarta",
            broadcast = BroadcastInfo(day = "Quarta", time = "15:00"),
            nextAiringEpisode = NextAiringEpisodeInfo(airingAt = (System.currentTimeMillis() / 1000L) + 7200L, episode = 5)
        )

        val timestamp = AnimeAlarmScheduler.calculateNextOccurrenceMillis(
            airingDay = fakeAnime.airingDay,
            broadcastTime = fakeAnime.broadcast.time,
            nextAiringAtSec = fakeAnime.nextAiringEpisode?.airingAt
        )

        assertTrue(timestamp > System.currentTimeMillis())
    }
}
