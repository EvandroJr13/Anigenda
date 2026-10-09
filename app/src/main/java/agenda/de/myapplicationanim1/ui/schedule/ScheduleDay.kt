package agenda.de.myapplicationanim1.ui.schedule

import java.time.DayOfWeek
import java.time.LocalDate

enum class ScheduleDay(
    val displayNamePt: String,
    val displayNameEn: String,
    val apiFilter: String,
) {
    MONDAY("Segunda", "Monday", "monday"),
    TUESDAY("Terça", "Tuesday", "tuesday"),
    WEDNESDAY("Quarta", "Wednesday", "wednesday"),
    THURSDAY("Quinta", "Thursday", "thursday"),
    FRIDAY("Sexta", "Friday", "friday"),
    SATURDAY("Sábado", "Saturday", "saturday"),
    SUNDAY("Domingo", "Sunday", "sunday");

    companion object {
        fun today(): ScheduleDay {
            return when (LocalDate.now().dayOfWeek) {
                DayOfWeek.MONDAY -> MONDAY
                DayOfWeek.TUESDAY -> TUESDAY
                DayOfWeek.WEDNESDAY -> WEDNESDAY
                DayOfWeek.THURSDAY -> THURSDAY
                DayOfWeek.FRIDAY -> FRIDAY
                DayOfWeek.SATURDAY -> SATURDAY
                DayOfWeek.SUNDAY -> SUNDAY
            }
        }
    }
}
