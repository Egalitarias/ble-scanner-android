package com.example.blescanner

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.blescanner.ui.theme.BleScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BleScannerTheme {
                ScannerApp(this)
            }
        }
    }
}

@Composable
private fun ScannerApp(activity: MainActivity) {
    val context = LocalContext.current
    val devices by BleScanRepository.devices.collectAsState()
    val isScanning by BleScanRepository.isScanning.collectAsState()
    var minRssi by remember { mutableFloatStateOf(-90f) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissions = remember { requiredBlePermissions() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val hasRequired = permissions.all { granted[it] == true || hasPermission(context, it) }
        if (hasRequired) {
            showPermissionRationale = false
            permissionDenied = false
            BleScanForegroundService.start(context)
        } else {
            permissionDenied = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isScanning) {
                    stringResource(R.string.status_scanning)
                } else {
                    stringResource(R.string.status_stopped)
                },
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (hasAllBlePermissions(context)) {
                        showPermissionRationale = false
                        permissionDenied = false
                        BleScanForegroundService.start(context)
                    } else {
                        showPermissionRationale = permissions.any {
                            activity.shouldShowRequestPermissionRationale(it)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }) {
                    Text(text = stringResource(R.string.start_scan))
                }
                OutlinedButton(onClick = { BleScanForegroundService.stop(context) }) {
                    Text(text = stringResource(R.string.stop_scan))
                }
            }

            if (showPermissionRationale) {
                Text(
                    text = stringResource(R.string.permissions_rationale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (permissionDenied) {
                Text(
                    text = stringResource(R.string.permissions_denied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(text = stringResource(R.string.rssi_filter, minRssi.toInt()))
            Slider(
                value = minRssi,
                onValueChange = { minRssi = it },
                valueRange = -90f..-40f
            )

            val filtered = remember(devices, minRssi) {
                devices.filter { it.rssi >= minRssi.toInt() }
            }

            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_devices),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.address }) { beacon ->
                        BeaconCard(beacon = beacon)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScannerPreview() {
    BleScannerTheme {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Device: Demo Beacon")
                Text("MAC: 01:23:45:67:89:AB")
                Text("RSSI: -54 dBm")
                Text("UUID: 12345678-1234-1234-1234-1234567890ab")
                Text("Major: 100")
                Text("Minor: 3")
            }
        }
    }
}
