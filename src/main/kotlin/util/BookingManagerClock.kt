package util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingManagerClock : UtcEpoch {
    override fun epochSecondNow() = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

    companion object {
        fun toLocalDateTime(epochSecond: Long) : LocalDateTime =
            LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC)

        fun toLocalDate(epochSecond: Long) : LocalDate =
            toLocalDateTime(epochSecond).toLocalDate()
    }
}
