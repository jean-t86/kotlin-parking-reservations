import java.time.LocalDate

class BookingManager(carPark: CarPark) {

    private val bookings: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    fun book(registrationPlate: String, date: LocalDate): Boolean {
        if (bookings[date] == null) bookings[date] = mutableListOf()
        bookings[date]?.add(registrationPlate)

        return true
    }
}
