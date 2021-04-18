import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingManager(
    private val carPark: CarPark,
    private val bookingManagerClock: UtcEpoch
) {

    private val bookingsByDate: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    private val bookingsByLicensePlate: MutableMap<Customer, MutableList<Long>> = mutableMapOf()

    fun book(booking: Booking): Boolean {
        val (bookingDate, customer) = booking
        val bookingCreationTimestamp = bookingManagerClock.epochSecondNow()

        // Check if car park is fully book on that date
        if (isCarParkFullyBooked(bookingDate)) return false

        // Check if customer is trying to double book on the same day
        if (hasCustomerAlreadyBookedForThatDate(booking)) return false

        // Check if customer is trying to make more than one booking on the same day
        if (isCustomerTryingToMakeMoreBookingsOnTheSameDay(customer, bookingCreationTimestamp)) return false

        if (bookingsByDate[bookingDate] == null) bookingsByDate[bookingDate] = mutableListOf()
        bookingsByDate[bookingDate]?.add(customer.licensePlate)

        if (bookingsByLicensePlate[customer] == null) bookingsByLicensePlate[customer] = mutableListOf()
        bookingsByLicensePlate[customer]?.add(bookingCreationTimestamp)

        return true
    }

    private fun isCarParkFullyBooked(date: LocalDate) = bookingsByDate[date]?.size == carPark.maxBays

    private fun hasCustomerAlreadyBookedForThatDate(booking: Booking): Boolean {
        val bookings = bookingsByDate[booking.bookingDate]
        return bookings != null && bookings.contains(booking.customer.licensePlate)
    }

    private fun isCustomerTryingToMakeMoreBookingsOnTheSameDay(
        customer: Customer,
        bookingCreationTimestamp: Long
    ): Boolean {

        val currentBookingLocalDate = LocalDateTime
            .ofEpochSecond(
                bookingCreationTimestamp,
                0,
                ZoneOffset.UTC
            ).toLocalDate()

        val latestBookingTimestamp = bookingsByLicensePlate[customer]?.maxByOrNull { it }

        latestBookingTimestamp?.let {
            val latestBookingLocalDate =
                LocalDateTime.ofEpochSecond(
                    latestBookingTimestamp,
                    0,
                    ZoneOffset.UTC
                ).toLocalDate()
            return currentBookingLocalDate == latestBookingLocalDate
        }

        return false
    }

    fun getBookings(date: LocalDate): List<String> = bookingsByDate[date] ?: listOf()
}
