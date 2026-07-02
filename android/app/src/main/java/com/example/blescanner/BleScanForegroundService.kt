package com.example.blescanner

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class BleScanForegroundService : Service() {
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startScanning()
            ACTION_STOP -> stopScanningAndSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopScanning()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startScanning() {
        if (scanning) return
        if (!hasAllBlePermissions(this)) {
            BleScanRepository.setScanning(false)
            return
        }

        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return
        val scanner = adapter.bluetoothLeScanner ?: return

        bluetoothLeScanner = scanner
        createChannelIfNeeded()
        startForeground(NOTIFICATION_ID, buildNotification())

        scanner.startScan(
            null,
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build(),
            scanCallback
        )
        scanning = true
        BleScanRepository.setScanning(true)
    }

    private fun stopScanningAndSelf() {
        stopScanning()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        if (!scanning) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
        ) {
            scanning = false
            BleScanRepository.setScanning(false)
            return
        }
        bluetoothLeScanner?.stopScan(scanCallback)
        scanning = false
        BleScanRepository.setScanning(false)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleResult)
        }

        @SuppressLint("MissingPermission")
        private fun handleResult(result: ScanResult) {
            if (!hasAllBlePermissions(this@BleScanForegroundService)) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !hasPermission(this@BleScanForegroundService, android.Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                return
            }
            BleScanRepository.upsert(
                address = result.device.address,
                name = result.device.name,
                rssi = result.rssi,
                iBeacon = IBeaconParser.parse(result.scanRecord)
            )
        }
    }

    companion object {
        private const val CHANNEL_ID = "ble-scan"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "com.example.myapplication.action.START_BLE_SCAN"
        private const val ACTION_STOP = "com.example.myapplication.action.STOP_BLE_SCAN"

        fun start(context: Context) {
            val intent = Intent(context, BleScanForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BleScanForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
