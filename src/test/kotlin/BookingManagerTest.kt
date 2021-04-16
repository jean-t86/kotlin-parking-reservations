import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class BookingManagerTest {

    private lateinit var bookingManager: BookingManager

    @Before
    fun setup() {
        val carPark = CarPark(4)
        bookingManager = BookingManager(carPark)
    }

    @Test
    fun `can book when all bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)

        val actual = bookingManager.book("AC34F", bookingDate)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when three bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)

        val actual = bookingManager.book("LK657", bookingDate)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when two bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)
        bookingManager.book("LK657", bookingDate)

        val actual = bookingManager.book("UYS34Y", bookingDate)

        assertEquals(true, actual)
    }

    @Test
    fun `can book when one bay available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)
        bookingManager.book("LK657", bookingDate)
        bookingManager.book("UYS34Y", bookingDate)

        val actual = bookingManager.book("ZZ034", bookingDate)

        assertEquals(true, actual)
    }

    @Test
    fun `cannot book when no bays available`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)
        bookingManager.book("LK657", bookingDate)
        bookingManager.book("UYS34Y", bookingDate)
        bookingManager.book("ZZ034", bookingDate)

        val actual = bookingManager.book("PKU875", bookingDate)

        assertEquals(false, actual)
    }

    @Test
    fun `cannot book twice in the same day`() {
        val bookingDate = LocalDate.of(2021, 4, 16)
        bookingManager.book("AC34F", bookingDate)

        val actual = bookingManager.book("AC34F", bookingDate)

        assertEquals(true, actual)
    }
}
