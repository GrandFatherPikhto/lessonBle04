package com.grandfatherpikhto.blin.data

import android.bluetooth.le.ScanResult

data class BleScanResult( val device: BleDevice,
                          val isConnectable: Boolean = false,
                          val rssi: Int = 0
) {
    constructor(scanResult: ScanResult)
            : this( BleDevice(scanResult.device),
                    scanResult.isConnectable,
                    scanResult.rssi
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleScanResult

        if (device != other.device) return false
        if (isConnectable != other.isConnectable) return false
        if (rssi != other.rssi) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + isConnectable.hashCode()
        result = 31 * result + rssi
        return result
    }
}
