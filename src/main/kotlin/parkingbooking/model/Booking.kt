package parkingbooking.model

import java.time.ZonedDateTime

data class Booking(val bookingDate: ZonedDateTime, val customer: Customer)
