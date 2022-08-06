package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.ParcelUuid
import android.util.Log
import com.grandfatherpikhto.blin.data.*
import com.grandfatherpikhto.blin.idling.ConnectingIdling
import com.grandfatherpikhto.blin.idling.DisconnectingIdling
import com.grandfatherpikhto.blin.idling.ScanIdling
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.random.Random

class FakeBleManager : BleManagerInterface {

    private val logTag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private val mutableStateFlowScanState = MutableStateFlow(BleScanManager.State.Stopped)
    override val stateFlowScanState: StateFlow<BleScanManager.State>
        get() = mutableStateFlowScanState.asStateFlow()
    override val scanState: BleScanManager.State
        get() = mutableStateFlowScanState.value

    private val mutableScanResults = mutableListOf<BleScanResult>()
    private val mutableSharedFlowScanResult = MutableSharedFlow<BleScanResult>(replay = 100)
    override val sharedFlowScanResult: SharedFlow<BleScanResult>
        get() = mutableSharedFlowScanResult.asSharedFlow()
    override val scanResults: List<BleScanResult>
        get() = mutableScanResults.toList()

    private val mutableFlowScanError = MutableStateFlow(-1)
    override val stateFlowScanError: StateFlow<Int>
        get() = mutableFlowScanError.asStateFlow()
    override val scanError: Int
        get() = mutableFlowScanError.value

    private val mutableStateFlowConnectState = MutableStateFlow(BleGattManager.State.Disconnected)
    override val stateFlowConnectState: StateFlow<BleGattManager.State>
        get() = mutableStateFlowConnectState.asStateFlow()
    override val connectState: BleGattManager.State
        get() = mutableStateFlowConnectState.value

    private val mutableSharedFlowStateCode = MutableStateFlow(-1)
    override val sharedFlowConnectStateCode: SharedFlow<Int>
        get() = mutableSharedFlowStateCode.asStateFlow()

    private val mutableStateFlowBleGatt = MutableStateFlow<BleGatt?>(null)
    override val stateFlowGatt: StateFlow<BleGatt?>
        get() = mutableStateFlowBleGatt.asStateFlow()
    override val bluetoothGatt: BleGatt?
        get() = mutableStateFlowBleGatt.value

    private val mutableStateFlowBond = MutableStateFlow<BleBondState?>(null)
    override val stateFlowBondState: StateFlow<BleBondState?>
        get() = mutableStateFlowBond.asStateFlow()
    override val bondState: BleBondState?
        get() = mutableStateFlowBond.value

    override fun bondRequest(address: String): Boolean {
        TODO("Not yet implemented")
    }

    val scanIdling: ScanIdling = ScanIdling.getInstance(this)
    val connectingIdling: ConnectingIdling = ConnectingIdling.getInstance(this)
    val disconnectingIdling: DisconnectingIdling = DisconnectingIdling.getInstance(this)

    override fun startScan(
        addresses: List<String>,
        names: List<String>,
        services: List<String>,
        stopOnFind: Boolean,
        filterRepeatable: Boolean,
        stopTimeout: Long
    ): Boolean {
        Log.d(logTag, "startScan($scanState)")
        if (scanState != BleScanManager.State.Scanning) {
            mutableStateFlowScanState.tryEmit(BleScanManager.State.Scanning)
            scope.launch {
                (1..10).forEach { i ->
                    delay(Random.nextLong(200, 1000))
                    mutableSharedFlowScanResult.tryEmit(
                        BleScanResult(
                            BleDevice(Random.nextBytes(6)
                                .joinToString (":") { String.format("%02X", it) },
                                String.format("BLE_%02d", i),
                                if (Random.nextBoolean()) BluetoothDevice.BOND_BONDED else BluetoothDevice.BOND_NONE),
                            Random.nextBoolean(),
                            Random.nextInt(-100, 0)))
                    if (scanState != BleScanManager.State.Scanning) return@forEach
                }
                stopScan()
            }
        }

        return true
    }

    init {
        scope.launch {
            sharedFlowScanResult.collect {
                mutableScanResults.add(it)
            }
        }
    }


    override fun stopScan() {
        Log.d(logTag, "stopScan()")
        mutableStateFlowScanState.tryEmit(BleScanManager.State.Stopped)
    }

    override fun connect(address: String): BleGatt? {
        Log.d(logTag, "connect($address)")
        scope.launch {
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connecting)
            delay(Random.nextLong(300, 1500))
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connected)
            scanResults.find { it.device.address == address }?.let { scanResult ->
                val bleServices = mutableListOf<BluetoothGattService>()
                (0..Random.nextInt(1,3)).forEach { _ ->
                    val bluetoothGattService = BluetoothGattService(UUID.randomUUID(), 0)
                    (0..Random.nextInt(1,5)).forEach { _ ->
                        val characteristic = BluetoothGattCharacteristic(UUID.randomUUID(), 0, 0)
                        characteristic.value = Random.nextBytes(Random.nextInt(0, 10))
                        bluetoothGattService.addCharacteristic(characteristic)
                    }
                    bleServices.add(bluetoothGattService)
                }
                val bleGatt = BleGatt(scanResult.device, bleServices)
                mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connected)
                mutableStateFlowBleGatt.tryEmit(bleGatt)
            }
        }

        return null
    }

    override fun disconnect() {
        Log.d(logTag, "disconnect()")
        scope.launch {
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Disconnecting)
            delay(Random.nextLong(100, 500))
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Disconnected)
            mutableStateFlowBleGatt.tryEmit(null)
        }
    }
}