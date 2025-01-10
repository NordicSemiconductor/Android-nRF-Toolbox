package no.nordicsemi.android.toolbox.libs.core.data.hts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class HTSDataParserTest {

        @Test
        fun `test parse with all fields present`() {
            val byteArray = byteArrayOf(
                0x07.toByte(),            // Flags: Unit (Celsius), Timestamp, Temperature Type present
                0x00.toByte(), 0x00.toByte(), 0x80.toByte(), 0x3F.toByte(), // Temperature: 1.0 (IEEE 11073-32-bit float)
                0xE4.toByte(), 0x07.toByte(), 0x05.toByte(), 0x15.toByte(), // Timestamp: 2020-05-21 10:30:45
                0x0A.toByte(), 0x1E.toByte(), 0x2D.toByte(),
                0x02.toByte()             // Temperature Type: 2
            )

            val result = HTSDataParser.parse(byteArray)

            assertNotNull(result)
            result?.let {
                assertEquals(1.0f, it.temperature, 0.01f)
                assertEquals(TemperatureUnitData.CELSIUS, it.unit)
                assertNotNull(it.timestamp)
                assertEquals(2020, it.timestamp?.get(Calendar.YEAR))
                assertEquals(Calendar.MAY, it.timestamp?.get(Calendar.MONTH))
                assertEquals(21, it.timestamp?.get(Calendar.DATE))
                assertEquals(2, it.type)
            }
        }

        @Test
        fun `test parse without optional fields`() {
            val byteArray = byteArrayOf(
                0x00.toByte(),            // Flags: Unit (Celsius), No Timestamp, No Temperature Type
                0x00.toByte(), 0x00.toByte(), 0x80.toByte(), 0x3F.toByte() // Temperature: 1.0
            )

            val result = HTSDataParser.parse(byteArray)

            assertNotNull(result)
            result?.let {
                assertEquals(1.0f, it.temperature, 0.01f)
                assertEquals(TemperatureUnitData.CELSIUS, it.unit)
                assertNull(it.timestamp)
                assertNull(it.type)
            }
        }

        @Test
        fun `test parse with insufficient byte array`() {
            val byteArray = byteArrayOf(
                0x07.toByte()            // Flags indicating more data, but insufficient array size
            )

            val result = HTSDataParser.parse(byteArray)

            assertNull(result)
        }

        @Test
        fun `test parse with invalid flag`() {
            val byteArray = byteArrayOf(
                0x08.toByte(),            // Invalid Flag
                0x00.toByte(), 0x00.toByte(), 0x80.toByte(), 0x3F.toByte() // Temperature: 1.0
            )

            val result = HTSDataParser.parse(byteArray)

            assertNull(result) // Should return null due to invalid unit flag
        }
    }
