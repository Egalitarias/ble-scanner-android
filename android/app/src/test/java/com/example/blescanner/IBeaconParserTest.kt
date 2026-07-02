package com.example.blescanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class IBeaconParserTest {
    @Test
    fun parseManufacturerData_returnsIBeacon_forValidPayload() {
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val data = manufacturerData(uuid = uuid, major = 100, minor = 3)

        val parsed = IBeaconParser.parseManufacturerData(data)

        assertEquals(uuid.toString(), parsed?.uuid)
        assertEquals(100, parsed?.major)
        assertEquals(3, parsed?.minor)
    }

    @Test
    fun parseManufacturerData_returnsNull_whenPayloadMissing() {
        assertNull(IBeaconParser.parseManufacturerData(null))
    }

    @Test
    fun parseManufacturerData_returnsNull_whenPayloadTooShort() {
        assertNull(IBeaconParser.parseManufacturerData(ByteArray(22)))
    }

    @Test
    fun parseManufacturerData_returnsNull_whenPrefixInvalid() {
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val data = manufacturerData(uuid = uuid, major = 1, minor = 2).also {
            it[0] = 0x01
        }

        assertNull(IBeaconParser.parseManufacturerData(data))
    }

    private fun manufacturerData(uuid: UUID, major: Int, minor: Int): ByteArray {
        val data = ByteArray(23)
        data[0] = 0x02
        data[1] = 0x15
        val uuidBytes = uuidToBytes(uuid)
        uuidBytes.copyInto(data, destinationOffset = 2)
        data[18] = ((major shr 8) and 0xFF).toByte()
        data[19] = (major and 0xFF).toByte()
        data[20] = ((minor shr 8) and 0xFF).toByte()
        data[21] = (minor and 0xFF).toByte()
        data[22] = (-59).toByte()
        return data
    }

    private fun uuidToBytes(uuid: UUID): ByteArray {
        val bytes = ByteArray(16)
        var msb = uuid.mostSignificantBits
        var lsb = uuid.leastSignificantBits
        for (index in 7 downTo 0) {
            bytes[index] = (msb and 0xFF).toByte()
            msb = msb ushr 8
        }
        for (index in 15 downTo 8) {
            bytes[index] = (lsb and 0xFF).toByte()
            lsb = lsb ushr 8
        }
        return bytes
    }
}
