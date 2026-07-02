package com.example.blescanner

import android.Manifest
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test

class BlePermissionsTest {
    @Test
    fun requiredBlePermissions_includesLocationOnly_belowAndroid12() {
        val permissions = requiredBlePermissions(sdkInt = Build.VERSION_CODES.R)

        assertEquals(
            listOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissions
        )
    }

    @Test
    fun requiredBlePermissions_includesBluetoothAndLocation_onAndroid12() {
        val permissions = requiredBlePermissions(sdkInt = Build.VERSION_CODES.S)

        assertEquals(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissions
        )
    }

    @Test
    fun requiredBlePermissions_includesNotifications_onAndroid13AndAbove() {
        val permissions = requiredBlePermissions(sdkInt = Build.VERSION_CODES.TIRAMISU)

        assertEquals(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            permissions
        )
    }
}
