package parkingbooking

import parkingbooking.exceptions.*
import parkingbooking.model.Booking
import parkingbooking.model.CarPark
import parkingbooking.model.Customer
import parkingbooking.util.BookingManagerClock
import parkingbooking.util.UtcEpoch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class BookingManager(
    private val carPark: CarPark,
    private val bookingManagerClock: UtcEpoch
) {
    private val bookingsDateToBooking: MutableMap<LocalDate, MutableList<Booking>> = mutableMapOf()

    private val bookingsCustomerToBookingTimestamp: MutableMap<Customer, MutableList<Long>> = mutableMapOf()

    fun book(booking: Booking): Boolean {
        val (bookingDate, customer) = booking
        val bookingCreationTimestamp = bookingManagerClock.epochSecondNow()

        when {
            isBookingInThePast(bookingDate.toLocalDate(), bookingCreationTimestamp) ->
                throw BookingInThePastException()

            isCarParkFullyBooked(bookingDate.toLocalDate()) ->
                throw ParkingFullyBookedException()

            hasCustomerAlreadyBookedForThatDate(booking) ->
                throw AlreadyBookedForThatDateException()

            isCustomerNotMakingABookingAtLeastOneDayAhead(bookingDate.toLocalDate(), bookingCreationTimestamp) ->
                throw BookOneDayAheadException()

            isCustomerTryingToMakeMoreBookingsInLessThan24Hours(customer, bookingCreationTimestamp) ->
                throw BookingAgainWithin24hrsException()

            else -> {
                if (bookingsDateToBooking[bookingDate.toLocalDate()] == null) {
                    bookingsDateToBooking[bookingDate.toLocalDate()] = mutableListOf()
                }
                bookingsDateToBooking[bookingDate.toLocalDate()]?.add(booking)

                if (bookingsCustomerToBookingTimestamp[customer] == null) {
                    bookingsCustomerToBookingTimestamp[customer] = mutableListOf()
                }
                bookingsCustomerToBookingTimestamp[customer]?.add(bookingCreationTimestamp)
            }
        }

        return true
    }

    private fun isBookingInThePast(bookingDate: LocalDate, bookingCreationTimestamp: Long): Boolean {
        val bookingCreationLocalDate = BookingManagerClock.toLocalDate(bookingCreationTimestamp)
        return bookingDate < bookingCreationLocalDate
    }

    private fun isCustomerNotMakingABookingAtLeastOneDayAhead(
        bookingDate: LocalDate,
        bookingCreationTimestamp: Long
    ): Boolean {
        val bookingCreationLocalDate = BookingManagerClock.toLocalDate(bookingCreationTimestamp)
        return ChronoUnit.DAYS.between(bookingDate, bookingCreationLocalDate) == 0L
    }

    private fun isCarParkFullyBooked(date: LocalDate) = bookingsDateToBooking[date]?.size == carPark.maxBays

    private fun hasCustomerAlreadyBookedForThatDate(booking: Booking): Boolean {
        val bookings = bookingsDateToBooking[booking.bookingDate.toLocalDate()]

        return bookings != null && bookings.any {
            it.customer.licensePlate == booking.customer.licensePlate
        }
    }

    private fun isCustomerTryingToMakeMoreBookingsInLessThan24Hours(
        customer: Customer,
        bookingCreationTimestamp: Long
    ): Boolean {
        val currentBookingLocalDate = BookingManagerClock.toLocalDateTime(bookingCreationTimestamp)
        val latestBookingTimestamp = bookingsCustomerToBookingTimestamp[customer]?.maxByOrNull { it }

        latestBookingTimestamp?.let {
            val latestBookingLocalDate = BookingManagerClock.toLocalDateTime(latestBookingTimestamp)
            return ChronoUnit.HOURS.between(latestBookingLocalDate, currentBookingLocalDate) < 24
        }

        return false
    }

    fun getBookings(date: ZonedDateTime): List<Booking> = bookingsDateToBooking[date.toLocalDate()] ?: listOf()
}
