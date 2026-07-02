package com.example.blescanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.blescanner.BeaconDevice
import com.example.blescanner.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun BeaconCard(beacon: BeaconDevice) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = beacon.name?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.unknown_device_name),
                style = MaterialTheme.typography.titleSmall
            )
            Text(text = "MAC: ${beacon.address}")
            Text(text = "RSSI: ${beacon.rssi} dBm")
            if (beacon.iBeacon != null) {
                Text(text = "UUID: ${beacon.iBeacon.uuid}")
                Text(
                    text = String.format(
                        Locale.US,
                        "Major: %d  Minor: %d",
                        beacon.iBeacon.major,
                        beacon.iBeacon.minor
                    )
                )
            } else {
                Text(text = "iBeacon: Not detected")
            }
        }
    }
}
