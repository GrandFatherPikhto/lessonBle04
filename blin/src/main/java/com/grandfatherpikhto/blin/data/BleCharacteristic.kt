package com.grandfatherpikhto.blin.data

import android.bluetooth.BluetoothGattCharacteristic
import android.os.ParcelUuid

class BleCharacteristic constructor(private val parcelUuid: ParcelUuid,
                                    val value: ByteArray = ByteArray(0)
) {
    constructor(bluetoothGattCharacteristic: BluetoothGattCharacteristic) : this (
        ParcelUuid(bluetoothGattCharacteristic.uuid),
        bluetoothGattCharacteristic.value ?: ByteArray(0))
    val uuid get() = parcelUuid.uuid
}