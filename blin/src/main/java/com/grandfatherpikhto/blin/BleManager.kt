package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.grandfatherpikhto.blin.idling.BleIdling
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleManager constructor(private val context: Context,
                             dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : BleManagerInterface {

    override val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    override val bluetoothAdapter: BluetoothAdapter
        = bluetoothManager.adapter
    override val bluetoothLeScanner: BluetoothLeScanner
        = bluetoothAdapter.bluetoothLeScanner

    override val applicationContext:Context get() = context.applicationContext

    val bleScanManager: BleScanManager = BleScanManager(this, dispatcher)
    val bleGattManager: BleGattManager = BleGattManager(this, dispatcher)
    val bleBondManager: BleBondManager = BleBondManager(this, dispatcher)

    override val stateFlowScanState get() = bleScanManager.stateFlowScanState
    override val scanState get()     = bleScanManager.scanState

    override val sharedFlowScanResult get() = bleScanManager.sharedFlowScanResult
    override val scanResults get() = bleScanManager.results

    override val stateFlowScanError get() = bleScanManager.stateFlowError
    override val scanError get()     = bleScanManager.scanError

    override val stateFlowConnectState get() = bleGattManager.stateFlowConnectState
    override val connectState get() = bleGattManager.connectState

    override val stateFlowConnectStateCode get() = bleGattManager.stateFlowConnectStateCode

    override val stateFlowGatt get() = bleGattManager.stateFlowGatt
    override val bluetoothGatt get() = bleGattManager.bluetoothGatt

    override val stateFlowBondState get() = bleBondManager.stateFlowBond
    override val stateBond get() = bleBondManager.stateBond

    override fun bondRequest(bluetoothDevice: BluetoothDevice)
        = bleBondManager.bondRequest(bluetoothDevice)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.addObserver(bleScanManager)
        owner.lifecycle.addObserver(bleGattManager)
        owner.lifecycle.addObserver(bleBondManager)
    }

    override
    fun startScan(addresses: List<String>,
                  names: List<String>,
                  services: List<String>,
                  stopOnFind: Boolean,
                  filterRepeatable: Boolean,
                  stopTimeout: Long
    ) : Boolean = bleScanManager.startScan( addresses, names, services,
                                            stopOnFind, filterRepeatable, stopTimeout )

    override fun stopScan() = bleScanManager.stopScan()

    override fun connect(address: String) = bleGattManager.connect(address)
    override fun disconnect() = bleGattManager.disconnect()

    override fun getScanIdling(name: String?): BleIdling = bleScanManager.getScanIdling(name)
    override fun getGattIdling():BleIdling = bleGattManager.getGattIdling()

}