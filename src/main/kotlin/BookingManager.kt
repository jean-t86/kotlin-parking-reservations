import java.time.LocalDate

class BookingManager(private val carPark: CarPark) {

    private val bookings: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    fun book(registrationPlate: String, date: LocalDate): Boolean {
        if (bookings[date] == null) bookings[date] = mutableListOf()
        if (bookings[date]?.size == carPark.maxBays) return false

        bookings[date]?.add(registrationPlate)
        return true
    }

    fun getBookings(date: LocalDate): List<String> = bookings[date] ?: listOf()
}
