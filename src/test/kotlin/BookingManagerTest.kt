import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class BookingManagerTest {

    private val now = LocalDate.now()

    private lateinit var bookingManager: BookingManager

    @Before
    fun setup() {
        val carPark = CarPark(4)
        bookingManager = BookingManager(carPark)
    }

    @Test
    fun `can book when all bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(now, bookingDate, "AC34F")

        val actual = bookingManager.book(booking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when three bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(now, bookingDate, "AC34F")
        bookingManager.book(booking)

        val actualBooking = Booking(now, bookingDate, "LK657")
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when two bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking1 = Booking(now, bookingDate, "AC34F")
        val booking2 = Booking(now, bookingDate, "LK657")
        bookingManager.book(booking1)
        bookingManager.book(booking2)

        val actualBooking = Booking(now, bookingDate, "UYS34Y")
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when one bay available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking1 = Booking(now, bookingDate, "AC34F")
        val booking2 = Booking(now, bookingDate, "LK657")
        val booking3 = Booking(now, bookingDate, "UYS34Y")
        bookingManager.book(booking1)
        bookingManager.book(booking2)
        bookingManager.book(booking3)

        val actualBooking = Booking(now, bookingDate, "ZZ034")
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `cannot book when no bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking1 = Booking(now, bookingDate, "AC34F")
        val booking2 = Booking(now, bookingDate, "LK657")
        val booking3 = Booking(now, bookingDate, "UYS34Y")
        val booking4 = Booking(now, bookingDate, "ZZ034")
        bookingManager.book(booking1)
        bookingManager.book(booking2)
        bookingManager.book(booking3)
        bookingManager.book(booking4)

        val actualBooking = Booking(now, bookingDate, "ZZ034")
        val actual = bookingManager.book(actualBooking)

        assertEquals(false, actual)
    }

    @Test
    fun `cannot book twice in the same day`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)

        val actual = bookingManager.book("AC34F", bookingDate)

        assertEquals(false, actual)
    }
}
