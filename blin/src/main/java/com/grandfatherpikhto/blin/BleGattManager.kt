package com.grandfatherpikhto.blin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleGattManager constructor(private val bleManager: BleManager,
                                 private val dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : DefaultLifecycleObserver {
    enum class State(val value:Int) {
        Disconnected  (0x00), // Отключены
        Disconnecting (0x01), // Отключаемся
        Connecting    (0x02), // Подключаемся
        Connected     (0x02), // Подключены
        Error         (0xFF), // Получена ошибка
    }

    enum class Connection(val value: Int) {
        Disconnected(0x00),
        Disconnecting(0x01),
        Connecting(0x02),
        Connected(0x03),
        Discovering(0x04),
        Discovered(0x05),
        Rescanning(0x06),
        Rescanned(0x07),
        Error(0xFF)
    }

    private val logTag = this.javaClass.simpleName
    private val bleGattCallback  = BleGattCallback(this, dispatcher)
    private var bluetoothDevice: BluetoothDevice? = null
    val device get() = bluetoothDevice
    private var scope = CoroutineScope(dispatcher)

    private var attemptReconnect = true
    private var reconnectAttempts = 0
    val attempt get() = reconnectAttempts
    private val maxAttempts = 6

    private val mutableStateFlowConnectState  = MutableStateFlow(State.Disconnected)
    val stateFlowConnectState get() = mutableStateFlowConnectState.asStateFlow()
    val connectState get() = mutableStateFlowConnectState.value

    private val mutableStateFlowConnectStateCode = MutableStateFlow(-1)
    val stateFlowConnectStateCode get() = mutableStateFlowConnectStateCode.asStateFlow()
    val connectStateCode get() = mutableStateFlowConnectState.value

    private val mutableStateFlowGatt = MutableStateFlow<BluetoothGatt?>(null)
    val stateFlowGatt get() = mutableStateFlowGatt.asStateFlow()
    val bluetoothGatt:BluetoothGatt? get() = mutableStateFlowGatt.value

    val bleScanManager = bleManager.scanner

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        scope.launch {
            bleScanManager.stateFlowScanState.collect { scanState ->
                if (attemptReconnect &&
                    scanState == BleScanManager.State.Stopped &&
                        bleScanManager.results.last().device == bluetoothDevice) {
                    if (reconnectAttempts < maxAttempts) {
                        doConnect()
                    } else {
                        mutableStateFlowConnectState.tryEmit(State.Error)
                        attemptReconnect = false
                    }
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disconnect()
        super.onDestroy(owner)
    }

    /**
     *
     */
    private fun doRescan() {
        Log.d(logTag, "doRescan()")
        if (attemptReconnect && reconnectAttempts < maxAttempts) {
            bluetoothDevice?.let { device ->
                bleManager.startScan(addresses = listOf(device.address),
                    stopTimeout = 2000L,
                    stopOnFind = true)
            }
        }
    }

    fun connect(address:String) : BluetoothGatt? {
        Log.d(logTag, "connect($address)")
        bleManager.bluetoothAdapter.getRemoteDevice(address)?.let { device ->
            mutableStateFlowConnectState.tryEmit(State.Connecting)
            bluetoothDevice = device
            attemptReconnect = true
            reconnectAttempts = 0
            doConnect()
        }

        return null
    }

    @SuppressLint("MissingPermission")
    private fun doConnect() : BluetoothGatt? {
        bluetoothDevice?.let { device ->
            Log.d(logTag, "doConnect($device), reconnect = $attemptReconnect, attempts = $reconnectAttempts")
            if (attemptReconnect) {
                reconnectAttempts ++
            }
            return device.connectGatt(
                bleManager.applicationContext,
                device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN,
                bleGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }

        return null
    }

    /**
     * Даёт команду на отключение (gatt.disconnect()).
     * Если статус Disconnected, сразу закрывает подключение gatt.close()
     * Если нет, блокирует поток и ждёт, пока не будет получено состояние
     * Disconnected и после этого закрыает подключение
     * Это нужно для того, чтобы сбросить счётчик подключений
     * Если он переполнится, нужно будет очищать кэш Bluetooth
     */
    fun disconnect() {
        Log.d(logTag, "disconnect()")
        attemptReconnect = false
        reconnectAttempts = 0
        mutableStateFlowConnectState.tryEmit(State.Disconnecting)
        doDisconnect()
    }

    @SuppressLint("MissingPermission")
    private fun doDisconnect() = runBlocking {
        bluetoothGatt?.let { gatt ->
            Log.d(logTag, "doDisconnect($bluetoothDevice)")
            gatt.disconnect()
            while (connectState != State.Disconnected) {
                delay(100)
            }
            gatt.close()
        }
    }

    /**
     * Интерфейсы исследованы. Сбрасываем счётчик переподключений и
     * генерируем событие о полном подключении. Можно принимать и передавать данные
     */
    fun onGattDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
        Log.d(logTag, "onGattDiscovered(status = $status)")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            discoveredGatt?.let {
                mutableStateFlowGatt.tryEmit(it)
                mutableStateFlowConnectState.tryEmit(State.Connected)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothProfile.STATE_DISCONNECTED -> {
                    mutableStateFlowConnectState.tryEmit(State.Disconnected)
                    mutableStateFlowGatt.tryEmit(null)
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt?.let {
                        if(!it.discoverServices()) {
                            mutableStateFlowConnectState.tryEmit(State.Error)
                            attemptReconnect  = false
                            reconnectAttempts = 0
                            doDisconnect()
                        }
                    }
                }
                else -> {
                    // Log.e(logTag, "Unknown newState: $newState")
                }
            }
        } else {
            mutableStateFlowConnectStateCode.tryEmit(newState)
            if (attemptReconnect) {
                if (reconnectAttempts < maxAttempts) {
                    doRescan()
                } else {
                    mutableStateFlowConnectState.tryEmit(State.Error)
                    attemptReconnect  = false
                    reconnectAttempts = 0
                }
            }
        }
    }
}