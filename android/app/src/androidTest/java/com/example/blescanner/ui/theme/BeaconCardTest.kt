package com.example.blescanner.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.blescanner.BeaconCard
import com.example.blescanner.BeaconDevice
import com.example.blescanner.IBeaconData
import org.junit.Rule
import org.junit.Test

class BeaconCardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsBeaconDetails() {
        composeRule.setContent {
            BeaconCard(
                beacon = BeaconDevice(
                    address = "00:11:22:33:44:55",
                    name = "Demo beacon",
                    rssi = -42,
                    iBeacon = IBeaconData(
                        uuid = "123e4567-e89b-12d3-a456-426614174000",
                        major = 1,
                        minor = 2
                    )
                )
            )
        }

        composeRule.onNodeWithText("Demo beacon").assertIsDisplayed()
        composeRule.onNodeWithText("MAC: 00:11:22:33:44:55").assertIsDisplayed()
        composeRule.onNodeWithText("RSSI: -42 dBm").assertIsDisplayed()
        composeRule.onNodeWithText("UUID: 123e4567-e89b-12d3-a456-426614174000").assertIsDisplayed()
        composeRule.onNodeWithText("Major: 1  Minor: 2").assertIsDisplayed()
    }

    @Test
    fun showsNotDetected_whenIBeaconMissing() {
        composeRule.setContent {
            BeaconCard(
                beacon = BeaconDevice(
                    address = "AA:BB:CC:DD:EE:FF",
                    name = "No iBeacon",
                    rssi = -70,
                    iBeacon = null
                )
            )
        }

        composeRule.onNodeWithText("No iBeacon").assertIsDisplayed()
        composeRule.onNodeWithText("iBeacon: Not detected").assertIsDisplayed()
    }
}
