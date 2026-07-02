package com.example.blescanner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BleScanRepository {
    private val _devicesByAddress = linkedMapOf<String, BeaconDevice>()
    private val _devices = MutableStateFlow<List<BeaconDevice>>(emptyList())
    private val _isScanning = MutableStateFlow(false)

    val devices: StateFlow<List<BeaconDevice>> = _devices.asStateFlow()
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun setScanning(isScanning: Boolean) {
        _isScanning.value = isScanning
    }

    fun upsert(address: String, name: String?, rssi: Int, iBeacon: IBeaconData?) {
        val existing = _devicesByAddress[address]
        _devicesByAddress[address] = BeaconDevice(
            address = address,
            name = name ?: existing?.name,
            rssi = rssi,
            iBeacon = iBeacon ?: existing?.iBeacon
        )
        _devices.value = _devicesByAddress.values.sortedByDescending { it.rssi }
    }
}
