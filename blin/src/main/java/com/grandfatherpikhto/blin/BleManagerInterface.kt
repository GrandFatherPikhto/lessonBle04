package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import com.grandfatherpikhto.blin.idling.BleIdling
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleManagerInterface : DefaultLifecycleObserver {
    val bluetoothManager: BluetoothManager
    val bluetoothAdapter: BluetoothAdapter
    val bluetoothLeScanner: BluetoothLeScanner

    val applicationContext: Context

    val stateFlowScanState:StateFlow<BleScanManager.State>
    val scanState:BleScanManager.State

    val sharedFlowScanResult: SharedFlow<ScanResult>
    val scanResults:List<ScanResult>

    val stateFlowScanError: StateFlow<Int>
    val scanError: Int

    val stateFlowConnectState: StateFlow<BleGattManager.State>
    val connectState: BleGattManager.State

    val stateFlowConnectStateCode: SharedFlow<Int>

    val stateFlowGatt: StateFlow<BluetoothGatt?>
    val bluetoothGatt: BluetoothGatt?

    val stateFlowBondState: StateFlow<BleBondManager.State>
    val stateBond: BleBondManager.State

    fun bondRequest(bluetoothDevice: BluetoothDevice)

    fun startScan(addresses: List<String> = listOf(),
                  names: List<String> = listOf(),
                  services: List<String> = listOf(),
                  stopOnFind: Boolean = false,
                  filterRepeatable: Boolean = true,
                  stopTimeout: Long = 0L
    ) : Boolean

    fun stopScan()

    fun connect(address: String) : BluetoothGatt?
    fun disconnect()
}