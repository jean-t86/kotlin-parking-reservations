package parkingbooking

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

    private val bookingsByDate: MutableMap<LocalDate, MutableList<String>> = mutableMapOf()

    private val bookingsCustomerToCreationTimestamp: MutableMap<Customer, MutableList<Long>> = mutableMapOf()

    fun book(booking: Booking): Boolean {
        val (bookingDate, customer) = booking
        val bookingCreationTimestamp = bookingManagerClock.epochSecondNow()

        // Check if the booking is in the past
        if (isBookingInThePast(bookingDate.toLocalDate(), bookingCreationTimestamp)) return false

        // Check if car park is fully book on that date
        if (isCarParkFullyBooked(bookingDate.toLocalDate())) return false

        // Check if customer is trying to double book on the same day
        if (hasCustomerAlreadyBookedForThatDate(booking)) return false

        // Check if customer is making a booking 1 day in advance. Time here is irrelevant
        if (isCustomerNotMakingABookingAtLeastOneDayAhead(bookingDate.toLocalDate(), bookingCreationTimestamp)) return false

        // Check if customer is trying to make more than one booking on the same day
        if (isCustomerTryingToMakeMoreBookingsInLessThan24Hours(customer, bookingCreationTimestamp)) return false

        if (bookingsByDate[bookingDate.toLocalDate()] == null) bookingsByDate[bookingDate.toLocalDate()] =
            mutableListOf()
        bookingsByDate[bookingDate.toLocalDate()]?.add(customer.licensePlate)

        if (bookingsCustomerToCreationTimestamp[customer] == null) bookingsCustomerToCreationTimestamp[customer] =
            mutableListOf()
        bookingsCustomerToCreationTimestamp[customer]?.add(bookingCreationTimestamp)

        return true
    }

    private fun isBookingInThePast(bookingDate: LocalDate, bookingCreationTimestamp: Long): Boolean {
        val bookingCreationLocalDate = BookingManagerClock.toLocalDate(bookingCreationTimestamp)
        return bookingDate < bookingCreationLocalDate
    }

    private fun isCustomerNotMakingABookingAtLeastOneDayAhead(bookingDate: LocalDate, bookingCreationTimestamp: Long): Boolean {
        val bookingCreationLocalDate = BookingManagerClock.toLocalDate(bookingCreationTimestamp)
        return ChronoUnit.DAYS.between(bookingDate, bookingCreationLocalDate) == 0L
    }

    // A customer can book all-day parking at a car park with a given date if there is a free bay available.
    private fun isCarParkFullyBooked(date: LocalDate) = bookingsByDate[date]?.size == carPark.maxBays

    // A customer cannot book more than one bay on the same date
    private fun hasCustomerAlreadyBookedForThatDate(booking: Booking): Boolean {
        val bookings = bookingsByDate[booking.bookingDate.toLocalDate()]
        return bookings != null && bookings.contains(booking.customer.licensePlate)
    }

    // A customer can only make one booking a day regardless of when that date is
    private fun isCustomerTryingToMakeMoreBookingsInLessThan24Hours(
        customer: Customer,
        bookingCreationTimestamp: Long
    ): Boolean {
        val currentBookingLocalDate = BookingManagerClock.toLocalDateTime(bookingCreationTimestamp)
        val latestBookingTimestamp = bookingsCustomerToCreationTimestamp[customer]?.maxByOrNull { it }

        latestBookingTimestamp?.let {
            val latestBookingLocalDate = BookingManagerClock.toLocalDateTime(latestBookingTimestamp)
            return ChronoUnit.HOURS.between(currentBookingLocalDate, latestBookingLocalDate) < 24
        }

        return false
    }

    fun getBookings(date: LocalDate): List<String> = bookingsByDate[date] ?: listOf()
}
