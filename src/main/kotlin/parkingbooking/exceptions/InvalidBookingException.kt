package parkingbooking.exceptions


sealed class InvalidBookingException(message: String) : Exception(message)

class BookingInThePastException : InvalidBookingException(
    """
        You cannot make a booking in the past.
    """.trimIndent()
)

class ParkingFullyBookedException : InvalidBookingException(
    """
        The parking for that date is fully booked.
    """.trimIndent()
)

class AlreadyBookedForThatDateException : InvalidBookingException(
    """
        You already have a booking for that date.
    """.trimIndent()
)

class NotBookedOneDayAheadException : InvalidBookingException(
    """
        You need to book your car bay at lease one day in advance.
    """.trimIndent()
)

class BookingAgainWithin24hrs : InvalidBookingException(
    """
        You can only book once every 24hrs.
    """.trimIndent()
)
