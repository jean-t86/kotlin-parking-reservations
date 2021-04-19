package parkingbooking

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import parkingbooking.exceptions.*
import parkingbooking.model.Booking
import parkingbooking.model.CarPark
import parkingbooking.model.Customer
import parkingbooking.util.UtcEpoch
import java.time.ZoneId
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
    private val mockBookingManagerClock: UtcEpoch = mockk()

    private lateinit var bookingManager: BookingManager

    @Before
    fun setup() {
        bookingManager = BookingManager(
            carPark,
            mockBookingManagerClock
        )
    }

    private fun mockBookingManagerClock(
        daysOffset: Long = 0,
        hoursOffset: Long = 0,
        minutesOffset: Long = 0,
        secondsOffset: Long = 0,
        isInThePast: Boolean = true
    ) {
        if (isInThePast) {
            every { mockBookingManagerClock.epochSecondNow() } returns
                    ZonedDateTime
                        .now(ZoneId.of("UTC"))
                        .minusDays(daysOffset)
                        .minusHours(hoursOffset)
                        .minusMinutes(minutesOffset)
                        .minusSeconds(secondsOffset)
                        .toEpochSecond()
        } else {
            every { mockBookingManagerClock.epochSecondNow() } returns
                    ZonedDateTime
                        .now(ZoneId.of("UTC"))
                        .plusDays(daysOffset)
                        .plusHours(hoursOffset)
                        .plusMinutes(minutesOffset)
                        .plusSeconds(secondsOffset)
                        .toEpochSecond()

        }
    }

    private fun getBookingDate(
        daysOffset: Long = 0,
        hoursOffset: Long = 0,
        minutesOffset: Long = 0,
        secondsOffset: Long = 0,
        isInThePast: Boolean = false
    ): ZonedDateTime {
        return if (isInThePast) {
            ZonedDateTime
                .now(ZoneId.of("UTC"))
                .minusDays(daysOffset)
                .minusHours(hoursOffset)
                .minusMinutes(minutesOffset)
                .minusSeconds(secondsOffset)
        } else {
            ZonedDateTime
                .now(ZoneId.of("UTC"))
                .plusDays(daysOffset)
                .plusHours(hoursOffset)
                .plusMinutes(minutesOffset)
                .plusSeconds(secondsOffset)
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
    fun `cannot book twice for the same date `() {
        mockBookingManagerClock()
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

    @Test
    fun `cannot make a booking 1 day in the past`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(1, isInThePast = true), customerEd)

        assertFailsWith<BookingInThePastException>("You cannot make a booking in the past.") {
            bookingManager.book(booking)
        }
    }

    @Test
    fun `cannot make a booking 2 days in the past`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(1, isInThePast = true), customerEd)

        assertFailsWith<BookingInThePastException>("You cannot make a booking in the past.") {
            bookingManager.book(booking)
        }
    }

    @Test
    fun `cannot make a booking 50 days in the past`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(50, isInThePast = true), customerEd)

        assertFailsWith<BookingInThePastException>("You cannot make a booking in the past.") {
            bookingManager.book(booking)
        }
    }

    @Test
    fun `needs to book one day ahead`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(), customerEd)

        assertFailsWith<BookOneDayAheadException>("You need to book your car bay at lease one day in advance.") {
            bookingManager.book(booking)
        }
    }

    @Test
    fun `can only book once every 24hrs`() {
        mockBookingManagerClock(1)
        val booking = Booking(getBookingDate(), customerAlice)
        assertTrue { bookingManager.book(booking) }

        val booking2 = Booking(getBookingDate(6), customerAlice)
        assertFailsWith<BookingAgainWithin24hrs>("You can only book once every 24hrs.") {
            bookingManager.book(booking2)
        }
    }

    @Test
    fun `can only book once every 24hrs but tries to book at the 23hr mark`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(1), customerAlice)
        assertTrue { bookingManager.book(booking) }

        mockBookingManagerClock(hoursOffset = 23, isInThePast = false)
        val booking2 = Booking(getBookingDate(2), customerAlice)
        assertFailsWith<BookingAgainWithin24hrs>("You can only book once every 24hrs.") {
            bookingManager.book(booking2)
        }
    }

    @Test
    fun `can only book once every 24hrs but tries at the last second before being allowed to book again`() {
        mockBookingManagerClock()
        val booking = Booking(getBookingDate(1), customerAlice)
        assertTrue { bookingManager.book(booking) }

        mockBookingManagerClock(hoursOffset = 23, minutesOffset = 59, secondsOffset = 59, isInThePast = false)
        val booking2 = Booking(getBookingDate(3), customerAlice)
        assertFailsWith<BookingAgainWithin24hrs>("You can only book once every 24hrs.") {
            bookingManager.book(booking2)
        }
    }

    @Test
    fun `can book once 24hrs has passed`() {
        // Now is 19/04/2021 @ 12:49
        mockBookingManagerClock()

        // book for the 20th
        val booking = Booking(getBookingDate(1), customerAlice)
        assertTrue { bookingManager.book(booking) }

        mockBookingManagerClock(hoursOffset = 24, isInThePast = false)
        val booking2 = Booking(getBookingDate(3), customerAlice)
        assertTrue { bookingManager.book(booking2) }
    }
}
