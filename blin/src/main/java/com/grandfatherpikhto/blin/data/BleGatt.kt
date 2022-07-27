package com.grandfatherpikhto.blin.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import java.util.*

class BleGatt constructor(private val bleDevice: BleDevice, private val bleServices: List<BleService>){
    constructor(bluetoothGatt: BluetoothGatt) :
            this ( BleDevice(bluetoothGatt.device),
            bluetoothGatt.services.map { BleService(it) } )
    val services get() = bleServices.toList()
    val device   get() = bleDevice
}