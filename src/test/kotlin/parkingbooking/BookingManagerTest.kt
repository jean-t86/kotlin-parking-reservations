package parkingbooking

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import parkingbooking.exceptions.AlreadyBookedForThatDateException
import parkingbooking.exceptions.ParkingFullyBookedException
import parkingbooking.model.Booking
import parkingbooking.model.CarPark
import parkingbooking.model.Customer
import parkingbooking.util.BookingManagerClock
import parkingbooking.util.UtcEpoch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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

    private fun mockBookingManagerClock(daysFromNow: Long = 0, isInThePast: Boolean = true) {
        val mockManagerClock = mockk<UtcEpoch>()
        if (isInThePast) {
            every { mockManagerClock.epochSecondNow() } returns
                    LocalDateTime.now().minusDays(daysFromNow)
                        .toEpochSecond(ZoneOffset.UTC)
        } else {
            every { mockManagerClock.epochSecondNow() } returns
                    LocalDateTime.now().plusDays(daysFromNow)
                        .toEpochSecond(ZoneOffset.UTC)
        }

        bookingManager = BookingManager(carPark, mockManagerClock)
    }

    private fun getBookingDate(daysFromNow: Long = 0, isInThePast: Boolean = false): ZonedDateTime {
        return if (isInThePast) {
            ZonedDateTime
                .of(LocalDateTime.now(), ZoneId.of("UTC"))
                .minusDays(daysFromNow)
        } else {
            ZonedDateTime
                .of(LocalDateTime.now(), ZoneId.of("UTC"))
                .plusDays(daysFromNow)
        }
    }

    @Test
    fun `can book when all bays available`() {
        mockBookingManagerClock(1)
        val booking = Booking(getBookingDate(), customerAlice)

        val actual = bookingManager.book(booking)

        assertTrue { actual }
    }

    @Test
    fun `can book when three bays available`() {
        mockBookingManagerClock(2)
        val booking = Booking(getBookingDate(), customerAlice)
        bookingManager.book(booking)

        val actualBooking = Booking(getBookingDate(), customerBob)
        val actual = bookingManager.book(actualBooking)

        assertTrue { actual }
    }

    @Test
    fun `can book when two bays available`() {
        mockBookingManagerClock(2)
        val bookingAlice = Booking(getBookingDate(), customerAlice)
        val bookingBob = Booking(getBookingDate(), customerBob)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)

        val actualBooking = Booking(getBookingDate(), customerCharlie)
        val actual = bookingManager.book(actualBooking)

        assertTrue { actual }
    }

    @Test
    fun `can book when one bay available`() {
        mockBookingManagerClock(2)
        val bookingAlice = Booking(getBookingDate(), customerAlice)
        val bookingBob = Booking(getBookingDate(), customerBob)
        val bookingCharlie = Booking(getBookingDate(), customerCharlie)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)
        bookingManager.book(bookingCharlie)

        val actualBooking = Booking(getBookingDate(), customerDave)
        val actual = bookingManager.book(actualBooking)

        assertTrue { actual }
    }

    @Test
    fun `cannot book when no bays available`() {
        mockBookingManagerClock(2)
        val bookingAlice = Booking(getBookingDate(), customerAlice)
        val bookingBob = Booking(getBookingDate(), customerBob)
        val bookingCharlie = Booking(getBookingDate(), customerCharlie)
        val bookingDave = Booking(getBookingDate(), customerDave)
        bookingManager.book(bookingAlice)
        bookingManager.book(bookingBob)
        bookingManager.book(bookingCharlie)
        bookingManager.book(bookingDave)

        assertFailsWith<ParkingFullyBookedException>("The parking for that date is fully booked.") {
            val actualBooking = Booking(getBookingDate(), customerEd)
            bookingManager.book(actualBooking)
        }
    }

    @Test
    fun `cannot book twice for the same date`() {
        mockBookingManagerClock(1)
        val booking = Booking(getBookingDate(3), customerEd)
        bookingManager.book(booking)

        assertFailsWith<AlreadyBookedForThatDateException>("You already have a booking for that date.") {
            val actualBooking = Booking(getBookingDate(3), customerEd)
            bookingManager.book(actualBooking)
        }
    }

    @Test
    fun `can only make a booking once a day but is trying twice`() {
        mockBookingManagerClock(1)
        val booking = Booking(getBookingDate(), customerEd)
        assertTrue { bookingManager.book(booking) }

        assertFailsWith<AlreadyBookedForThatDateException>("You already have a booking for that date.") {
            bookingManager.book(Booking(getBookingDate(), customerEd))
        }
    }

    @Test
    fun `can only make a booking once a day but is trying thrice`() {
        mockBookingManagerClock(1)
        assertTrue { bookingManager.book(Booking(getBookingDate(1), customerEd)) }

        assertFailsWith<AlreadyBookedForThatDateException>("You already have a booking for that date.") {
            bookingManager.book(Booking(getBookingDate(1), customerEd))
        }

        assertFailsWith<AlreadyBookedForThatDateException>("You already have a booking for that date.") {
            bookingManager.book(Booking(getBookingDate(1), customerEd))
        }
    }

    @Test
    fun `can only make a booking once a day and is doing so`() {
        mockBookingManagerClock(1)
        val booking = Booking(getBookingDate(4), customerEd)

        assertTrue { bookingManager.book(booking) }
    }
}
