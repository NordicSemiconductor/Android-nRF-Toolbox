package no.nordicsemi.android.csc

import androidx.annotation.FloatRange
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        println("red: ${colorToHex(0f)}")
        println("green: ${colorToHex(169f)}")
        println("blue: ${colorToHex(206f)}")
        assertEquals(4, 2 + 2)
    }

    private fun colorToHex(@FloatRange(from = 0.0, to = 1.0) value: Float) = Integer.toHexString((0xFF * value).toInt())
}