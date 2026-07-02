package com.example.blescanner

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat

fun requiredBlePermissions(sdkInt: Int = Build.VERSION.SDK_INT): List<String> {
    val permissions = mutableListOf<String>()
    if (sdkInt >= Build.VERSION_CODES.S) {
        permissions += Manifest.permission.BLUETOOTH_SCAN
        permissions += Manifest.permission.BLUETOOTH_CONNECT
    }
    permissions += Manifest.permission.ACCESS_FINE_LOCATION
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        permissions += Manifest.permission.POST_NOTIFICATIONS
    }
    return permissions
}

fun hasAllBlePermissions(context: Context): Boolean = requiredBlePermissions().all { permission ->
    hasPermission(context, permission)
}

fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
