import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingManager(private val carPark: CarPark) {

    private val bookingsByDate: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    private val bookingsByLicensePlate: MutableMap<Customer, MutableList<Long>> = mutableMapOf()

    fun book(booking: Booking): Boolean {
        val (bookingDate, customer) = booking
        val bookingDateTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

        // Check if car park is fully book on that date
        if (isCarParkFullyBooked(bookingDate)) return false

        // Check if customer is trying to double book on the same day
        if (hasCustomerAlreadyBookedThatDay(booking)) return false

        if (bookingsByDate[bookingDate] == null) bookingsByDate[bookingDate] = mutableListOf()
        bookingsByDate[bookingDate]?.add(customer.licensePlate)

        if (bookingsByLicensePlate[customer] == null) bookingsByLicensePlate[customer] = mutableListOf()
        bookingsByLicensePlate[customer]?.add(bookingDateTime)

        return true
    }

    private fun isCarParkFullyBooked(date: LocalDate) = bookingsByDate[date]?.size == carPark.maxBays

    private fun hasCustomerAlreadyBookedThatDay(booking: Booking): Boolean {
        val bookings = bookingsByDate[booking.bookingDate]
        return bookings != null && bookings.contains(booking.customer.licensePlate)
    }

    fun getBookings(date: LocalDate): List<String> = bookingsByDate[date] ?: listOf()
}
