import java.time.LocalDate

class BookingManager(carPark: CarPark) {

    private val bookings: Map<LocalDate, Int> = mutableMapOf()

    fun book(registrationPlate: String, date: LocalDate): Boolean {
        return false
    }
}
