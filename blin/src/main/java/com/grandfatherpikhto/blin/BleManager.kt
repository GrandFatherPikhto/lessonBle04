package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.grandfatherpikhto.blin.data.BleGatt
import com.grandfatherpikhto.blin.data.BleScanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class BleManager constructor(private val context: Context,
                             dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : BleManagerInterface {

    private val logTag = this.javaClass.simpleName
    private val scope = CoroutineScope(dispatcher)

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter
        = bluetoothManager.adapter
    val bluetoothLeScanner: BluetoothLeScanner
        = bluetoothAdapter.bluetoothLeScanner

    val applicationContext:Context get() = context.applicationContext

    val bleScanManager: BleScanManager = BleScanManager(this, dispatcher)
    val bleGattManager: BleGattManager = BleGattManager(this, dispatcher)
    val bleBondManager: BleBondManager = BleBondManager(this, dispatcher)

    override val stateFlowScanState get() = bleScanManager.stateFlowScanState
    override val scanState get()     = bleScanManager.scanState

    override val sharedFlowScanResult get() = bleScanManager.sharedFlowScanResult.map {
        Log.d(logTag, "ScanResult: ${it.device.address}")
        BleScanResult(it)}.shareIn(scope, SharingStarted.Lazily, replay = 100)

    override val scanResults get() = bleScanManager.scanResults.map { BleScanResult(it) }

    override val stateFlowScanError get() = bleScanManager.stateFlowError
    override val scanError get()     = bleScanManager.scanError

    override val stateFlowConnectState get() = bleGattManager.stateFlowConnectState
    override val connectState get() = bleGattManager.connectState

    override val sharedFlowConnectStateCode get() = bleGattManager.stateFlowConnectStateCode

    override val stateFlowGatt get() = bleGattManager.stateFlowGatt
        .map { if (it == null ) null else BleGatt(it) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val bluetoothGatt get() = if (bleGattManager.bluetoothGatt == null)
            null
        else
            BleGatt(bleGattManager.bluetoothGatt!!)

    override val stateFlowBondState get() = bleBondManager.stateFlowBond
    override val stateBond get() = bleBondManager.stateBond

    override fun bondRequest(address: String)
        = bleBondManager.bondRequest(address)

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

    override fun connect(address: String):BleGatt? {
        bleGattManager.connect(address)?.let {
            return BleGatt(it)
        }

        return null
    }

    override fun disconnect() = bleGattManager.disconnect()
}