import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.*
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
}
