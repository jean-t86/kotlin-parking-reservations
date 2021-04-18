package parkingbooking

import parkingbooking.exceptions.*
import parkingbooking.model.Booking
import parkingbooking.model.CarPark
import parkingbooking.model.Customer
import parkingbooking.util.BookingManagerClock
import parkingbooking.util.UtcEpoch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BookingManager(
    private val carPark: CarPark,
    private val bookingManagerClock: UtcEpoch
) {
    private val bookingsDateToCustomers: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

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
                throw NotBookedOneDayAheadException()

            isCustomerTryingToMakeMoreBookingsInLessThan24Hours(customer, bookingCreationTimestamp) ->
                throw BookingAgainWithin24hrs()

            else -> {
                if (bookingsDateToCustomers[bookingDate.toLocalDate()] == null) {
                    bookingsDateToCustomers[bookingDate.toLocalDate()] = mutableListOf()
                }
                bookingsDateToCustomers[bookingDate.toLocalDate()]?.add(customer.licensePlate)

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

    // A customer can book all-day parking at a car park with a given date if there is a free bay available.
    private fun isCarParkFullyBooked(date: LocalDate) = bookingsDateToCustomers[date]?.size == carPark.maxBays

    // A customer cannot book more than one bay on the same date
    private fun hasCustomerAlreadyBookedForThatDate(booking: Booking): Boolean {
        val bookings = bookingsDateToCustomers[booking.bookingDate.toLocalDate()]
        return bookings != null && bookings.contains(booking.customer.licensePlate)
    }

    // A customer can only make one booking a day regardless of when that date is
    private fun isCustomerTryingToMakeMoreBookingsInLessThan24Hours(
        customer: Customer,
        bookingCreationTimestamp: Long
    ): Boolean {
        val currentBookingLocalDate = BookingManagerClock.toLocalDateTime(bookingCreationTimestamp)
        val latestBookingTimestamp = bookingsCustomerToBookingTimestamp[customer]?.maxByOrNull { it }

        latestBookingTimestamp?.let {
            val latestBookingLocalDate = BookingManagerClock.toLocalDateTime(latestBookingTimestamp)
            return ChronoUnit.HOURS.between(currentBookingLocalDate, latestBookingLocalDate) < 24
        }

        return false
    }

    fun getBookings(date: LocalDate): List<String> = bookingsDateToCustomers[date] ?: listOf()
}
