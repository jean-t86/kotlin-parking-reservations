import java.time.LocalDate

class BookingManager(private val carPark: CarPark) {

    private val bookingsByDate: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    private val bookingsByLicensePlate: MutableMap<String, MutableList<LocalDate>> = mutableMapOf()

    fun book(booking: Booking): Boolean {
        val (creationDate, bookingDate, licensePlate) = booking

        if (bookingsByDate[bookingDate] == null) bookingsByDate[bookingDate] = mutableListOf()
        if (bookingsByDate[bookingDate]?.size == carPark.maxBays) return false

        bookingsByDate[bookingDate]?.add(licensePlate)
        return true
    }

    fun getBookings(date: LocalDate): List<String> = bookingsByDate[date] ?: listOf()
}
