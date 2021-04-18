package util

import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingManagerClock : UtcEpoch {
    override fun epochSecondNow() = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
}
