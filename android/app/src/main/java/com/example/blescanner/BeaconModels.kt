package com.example.blescanner

import android.bluetooth.le.ScanRecord
import java.util.UUID

data class IBeaconData(
    val uuid: String,
    val major: Int,
    val minor: Int,
)

data class BeaconDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    val iBeacon: IBeaconData?,
)

object IBeaconParser {
    private const val APPLE_COMPANY_ID = 0x004C

    fun parse(scanRecord: ScanRecord?): IBeaconData? {
        val manufacturerData = scanRecord?.getManufacturerSpecificData(APPLE_COMPANY_ID)
        return parseManufacturerData(manufacturerData)
    }

    internal fun parseManufacturerData(manufacturerData: ByteArray?): IBeaconData? {
        if (manufacturerData == null) return null
        if (manufacturerData.size < 23) return null
        if (manufacturerData[0].toInt() != 0x02 || manufacturerData[1].toInt() != 0x15) return null

        val uuidBytes = manufacturerData.copyOfRange(2, 18)
        val major = ((manufacturerData[18].toInt() and 0xFF) shl 8) or
                (manufacturerData[19].toInt() and 0xFF)
        val minor = ((manufacturerData[20].toInt() and 0xFF) shl 8) or
                (manufacturerData[21].toInt() and 0xFF)

        return IBeaconData(
            uuid = uuidFromBytes(uuidBytes).toString(),
            major = major,
            minor = minor
        )
    }

    private fun uuidFromBytes(uuidBytes: ByteArray): UUID {
        var msb = 0L
        var lsb = 0L
        for (index in 0..7) {
            msb = (msb shl 8) or (uuidBytes[index].toLong() and 0xFF)
        }
        for (index in 8..15) {
            lsb = (lsb shl 8) or (uuidBytes[index].toLong() and 0xFF)
        }
        return UUID(msb, lsb)
    }
}
