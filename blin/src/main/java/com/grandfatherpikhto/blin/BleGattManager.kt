package com.grandfatherpikhto.blin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.grandfatherpikhto.blin.idling.BleIdling
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class BleGattManager constructor(private val bleManager: BleManager,
                                 dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : DefaultLifecycleObserver {

    companion object {
        const val MAX_ATTEMPTS = 6
    }
    
    enum class State(val value:Int) {
        Disconnected  (0x00), // Отключены
        Disconnecting (0x01), // Отключаемся
        Connecting    (0x02), // Подключаемся
        Connected     (0x02), // Подключены
        Error         (0xFF), // Получена ошибка
    }

    private val logTag = this.javaClass.simpleName
    private val bleGattCallback  = BleGattCallback(this, dispatcher)
    private var bluetoothDevice: BluetoothDevice? = null
    val device get() = bluetoothDevice
    private var scope = CoroutineScope(dispatcher)

    private var attemptReconnect = true
    private var reconnectAttempts = 0
    val attempt get() = reconnectAttempts

    private val mutableStateFlowConnectState  = MutableStateFlow(State.Disconnected)
    val stateFlowConnectState get() = mutableStateFlowConnectState.asStateFlow()
    val connectState get() = mutableStateFlowConnectState.value

    private val mutableStateFlowConnectStateCode = MutableSharedFlow<Int>(replay = 100)
    val stateFlowConnectStateCode get() = mutableStateFlowConnectStateCode.asSharedFlow()

    private val mutableStateFlowGatt = MutableStateFlow<BluetoothGatt?>(null)
    val stateFlowGatt get() = mutableStateFlowGatt.asStateFlow()
    val bluetoothGatt:BluetoothGatt? get() = mutableStateFlowGatt.value

    val bleScanManager = bleManager.bleScanManager

    private var connectIdling: BleIdling? = null
    fun getGattIdling() : BleIdling {
        val idling = BleIdling.getInstance()
        if (connectIdling == null) {
            connectIdling = idling
            scope.launch {
                connectIdling?.let { idling ->
                    stateFlowConnectState.collect { state ->
                        if (state == State.Connected) {
                            idling.completed = true
                        }
                    }
                }
            }
        }
        return idling
    }


    init {
        scope.launch {
            bleScanManager.stateFlowScanState.collect { scanState ->
                if (attemptReconnect && bluetoothDevice != null &&
                    scanState == BleScanManager.State.Stopped &&
                    bleScanManager.results.isNotEmpty() &&
                    bleScanManager.results.last().device.address
                        == bluetoothDevice!!.address) {
                    if (reconnectAttempts < MAX_ATTEMPTS) {
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
        if (attemptReconnect && reconnectAttempts < MAX_ATTEMPTS) {
            bluetoothDevice?.let { device ->
                bleManager.startScan(addresses = listOf(device.address),
                    stopTimeout = 2000L,
                    stopOnFind = true)
            }
        }
    }

    fun connect(address:String) : BluetoothGatt? {
        Log.d(logTag, "connect($address)")
        if (connectState == State.Disconnected) {
            connectIdling?.completed = false
            bleManager.bluetoothAdapter.getRemoteDevice(address)?.let { device ->
                mutableStateFlowConnectState.tryEmit(State.Connecting)
                bluetoothDevice = device
                attemptReconnect = true
                reconnectAttempts = 0
                doConnect()
            }
        }

        return null
    }

    @SuppressLint("MissingPermission")
    private fun doConnect() : BluetoothGatt? {
        bluetoothDevice?.let { device ->
            reconnectAttempts ++

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
                if (reconnectAttempts < MAX_ATTEMPTS) {
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