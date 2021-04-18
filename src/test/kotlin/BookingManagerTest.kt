import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class BookingManagerTest {

    private val customerAlice = Customer("AC34F", "Alice")
    private val customerBob = Customer("LK657", "Bob")
    private val customerCharlie = Customer("UYS34Y", "Charlie")
    private val customerDave = Customer("ZZ034", "Charlie")
    private val customerEd = Customer("DD235Z", "Ed")

    private val carPark = CarPark(4)
    private val bookingManagerClock: UtcEpoch = BookingManagerClock()

    private lateinit var bookingManager: BookingManager

    @Before
    fun setup() {
        bookingManager = BookingManager(
            carPark,
            bookingManagerClock
        )
    }

    @Test
    fun `can book when all bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(bookingDate, customerAlice)

        val actual = bookingManager.book(booking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when three bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(bookingDate, customerAlice)
        bookingManager.book(booking)

        val actualBooking = Booking(bookingDate, customerBob)
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when two bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val bookingAlice = Booking(bookingDate, customerAlice)
        val bookingBob = Booking(bookingDate, customerBob)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)

        val actualBooking = Booking(bookingDate, customerCharlie)
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when one bay available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val bookingAlice = Booking(bookingDate, customerAlice)
        val bookingBob = Booking(bookingDate, customerBob)
        val bookingCharlie = Booking(bookingDate, customerCharlie)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)
        bookingManager.book(bookingCharlie)

        val actualBooking = Booking(bookingDate, customerDave)
        val actual = bookingManager.book(actualBooking)

        assertEquals(true, actual)
    }

    @Test
    fun `cannot book when no bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val bookingAlice = Booking(bookingDate, customerAlice)
        val bookingBob = Booking(bookingDate, customerBob)
        val bookingCharlie = Booking(bookingDate, customerCharlie)
        val bookingDave = Booking(bookingDate, customerDave)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)
        bookingManager.book(bookingCharlie)
        bookingManager.book(bookingDave)

        val actualBooking = Booking(bookingDate, customerEd)
        val actual = bookingManager.book(actualBooking)

        assertEquals(false, actual)
    }

    @Test
    fun `cannot book twice in the same day`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(bookingDate, customerEd)
        bookingManager.book(booking)

        val actualBooking = Booking(bookingDate, customerEd)
        val actual = bookingManager.book(actualBooking)

        assertEquals(false, actual)
    }

    @Test
    fun `can only make a booking once a day but is trying twice`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(bookingDate, customerEd)
        bookingManager.book(booking)

        val actualBookingDate = LocalDate.of(2021, 6, 17)
        val actualBooking = Booking(actualBookingDate, customerEd)
        val actual = bookingManager.book(actualBooking)

        assertEquals(false, actual)
    }

    @Test
    fun `can only make a booking once a day and is doing so`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        val booking = Booking(bookingDate, customerEd)

        val actual = bookingManager.book(booking)

        assertEquals(true, actual)
    }
}
