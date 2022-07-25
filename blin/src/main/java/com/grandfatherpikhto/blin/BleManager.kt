package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleManager constructor(private val context: Context,
                             dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : DefaultLifecycleObserver {
    val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    val applicationContext:Context get() = context.applicationContext
    private val bleScanManager = BleScanManager(this, dispatcher)
    val scanner get() = bleScanManager

    private val bleGattManager = BleGattManager(this, dispatcher)
    val connector get() = bleGattManager

    private val bleBondManager = BleBondManager(this, dispatcher)
    val bonder get() = bleGattManager

    val stateFlowScanState get() = bleScanManager.stateFlowScanState
    val scanState get()     = bleScanManager.stateFlowScanState.value

    val sharedFlowScanResult get() = bleScanManager.sharedFlowScanResult
    val scanResults get() = bleScanManager.results

    val stateFlowScanError get() = bleScanManager.stateFlowError
    val scanError get()     = bleScanManager.scanError

    val stateFlowConnectState get() = bleGattManager.stateFlowConnectState
    val connectState get() = bleGattManager.stateFlowConnectState.value

    val stateFlowConnectStateCode get() = bleGattManager.stateFlowConnectStateCode
    val connectStateCode get() = bleGattManager.connectStateCode

    val stateFlowGatt get() = bleGattManager.stateFlowGatt
    val bluetoothGatt get() = bleGattManager.bluetoothGatt

    val stateFlowBondState get() = bleBondManager.stateFlowBond
    val stateBonded get() = bleBondManager.stateBond

    fun bondRequest(bluetoothDevice: BluetoothDevice) = bleBondManager.bondRequest(bluetoothDevice)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.addObserver(bleScanManager)
        owner.lifecycle.addObserver(bleGattManager)
    }

    fun startScan(addresses: List<String> = listOf(),
                  names: List<String> = listOf(),
                  services: List<String> = listOf(),
                  stopOnFind: Boolean = false,
                  filterRepeatable: Boolean = true,
                  stopTimeout: Long = 0L
    ) : Boolean = bleScanManager.startScan(addresses, names, services,
        stopOnFind, filterRepeatable, stopTimeout)

    fun stopScan() = bleScanManager.stopScan()

    fun connect(address: String) = bleGattManager.connect(address)
    fun disconnect() = bleGattManager.disconnect()
}