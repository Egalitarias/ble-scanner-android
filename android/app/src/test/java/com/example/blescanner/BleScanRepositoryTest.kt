package com.example.blescanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BleScanRepositoryTest {
    @Test
    fun upsert_preservesExistingNameAndIBeacon_whenNewValuesAreNull() {
        val address = "AA:BB:CC:DD:EE:01"
        val initialBeacon = IBeaconData(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            major = 10,
            minor = 20
        )

        BleScanRepository.upsert(address = address, name = "Test beacon", rssi = -70, iBeacon = initialBeacon)
        BleScanRepository.upsert(address = address, name = null, rssi = -60, iBeacon = null)

        val updated = BleScanRepository.devices.value.firstOrNull { it.address == address }
        assertNotNull(updated)
        assertEquals("Test beacon", updated?.name)
        assertEquals(-60, updated?.rssi)
        assertEquals(initialBeacon, updated?.iBeacon)
    }

    @Test
    fun upsert_ordersDevicesByDescendingRssi() {
        val a = "AA:BB:CC:DD:EE:11"
        val b = "AA:BB:CC:DD:EE:12"
        val c = "AA:BB:CC:DD:EE:13"

        BleScanRepository.upsert(address = a, name = "A", rssi = -80, iBeacon = null)
        BleScanRepository.upsert(address = b, name = "B", rssi = -50, iBeacon = null)
        BleScanRepository.upsert(address = c, name = "C", rssi = -65, iBeacon = null)

        val filteredOrder = BleScanRepository.devices.value
            .filter { it.address == a || it.address == b || it.address == c }
            .map { it.address }

        assertEquals(listOf(b, c, a), filteredOrder)
    }
}
