package com.grandfatherpikhto.blin.data

import android.bluetooth.BluetoothGattService
import android.os.ParcelUuid

class BleService constructor(val parcelUuid: ParcelUuid,
                             private val bleCharacteristics: List<BleCharacteristic>){
    constructor(bluetoothService: BluetoothGattService) : this (
        ParcelUuid(bluetoothService.uuid),
        bluetoothService.characteristics.map { BleCharacteristic(it) })
    val uuid get() = parcelUuid.uuid
    val characteristics get() = bleCharacteristics.toList()
}