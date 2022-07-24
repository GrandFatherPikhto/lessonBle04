package com.grandfatherpikhto.blin

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.grandfatherpikhto.blin.idling.ScanIdling
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class BleScanManager constructor(private val bleManager: BleManager,
                                 ioDispatcher: CoroutineDispatcher = Dispatchers.IO)
    : DefaultLifecycleObserver {

    private val bcScanReceiver:BcScanReceiver = BcScanReceiver(this)

    val applicationContext: Context get() = bleManager.applicationContext

    enum class State (val value: Int) {
        Stopped(0x00),
        Scanning(0x01),
        Error(0x03)
    }

    private val logTag = this.javaClass.simpleName

    private val mutableStateFlowScanState = MutableStateFlow(State.Stopped)
    val stateFlowScanState get() = mutableStateFlowScanState.asStateFlow()
    private val state get() = mutableStateFlowScanState.value

    private val mutableSharedFlowScanResult = MutableSharedFlow<ScanResult>(replay = 100)
    val sharedFlowScanResult get() = mutableSharedFlowScanResult.asSharedFlow()

    private val mutableFlowStateError = MutableStateFlow<Int>(-1)
    val stateFlowError get() = mutableFlowStateError.asStateFlow()
    val scanError get() = mutableFlowStateError.value

    private var bleScanPendingIntent: PendingIntent = bcScanReceiver.pendingIntent

    private val scanFilters = mutableListOf<ScanFilter>()
    private val scanSettingsBuilder = ScanSettings.Builder()

    private var scope = CoroutineScope(ioDispatcher)
    private var notEmitRepeat: Boolean = true
    private val scanResults = mutableListOf<ScanResult>()
    val devices get() = scanResults.map { it.device }.toList()
    val results get() = scanResults.toList()

    private var stopOnFind = false
    private var stopTimeout = 0L

    private val addresses = mutableListOf<String>()
    private val names = mutableListOf<String>()
    private val uuids = mutableListOf<ParcelUuid>()

    private var scanIdling: ScanIdling? = null

    fun getScanIdling() : ScanIdling {
        val idling = ScanIdling.getInstance()
        if (scanIdling == null) {
            scanIdling = idling
            scope.launch {
                scanIdling?.let { idling ->
                    sharedFlowScanResult.collect {
                        idling.scanned = true
                    }
                }
            }
        }
        return idling
    }

    init {
        initScanSettings()
        initScanFilters()
    }

    @SuppressLint("MissingPermission")
    fun startScan(addresses: List<String> = listOf(),
                  names: List<String> = listOf(),
                  services: List<String> = listOf(),
                  stopOnFind: Boolean = false,
                  filterRepeatable: Boolean = true,
                  stopTimeout: Long = 0L
    ) : Boolean {
        if (state == State.Error) {
            Log.e(logTag, "Error: ${stateFlowError.value}")
            mutableStateFlowScanState.tryEmit(State.Stopped)
        }

        scanIdling?.scanned = false

        if (state == State.Stopped) {
            // devices.clear()

            Log.d(logTag, "startScan()")

            this.addresses.clear()
            this.addresses.addAll(addresses)

            this.names.clear()
            this.names.addAll(names)

            this.stopOnFind = stopOnFind
            this.notEmitRepeat = filterRepeatable

            this.uuids.clear()
            this.uuids.addAll(services.mapNotNull { ParcelUuid.fromString(it) }
                .toMutableList())

            if (stopTimeout > 0) {
                scope.launch {
                    this@BleScanManager.stopTimeout = stopTimeout
                    delay(stopTimeout)
                    stopScan()
                }
            }

            val result = bleManager.bluetoothLeScanner.startScan(
                scanFilters,
                scanSettingsBuilder.build(),
                bleScanPendingIntent
            )
            if (result == 0) {
                mutableStateFlowScanState.tryEmit(State.Scanning)
                return true
            } else {
                mutableStateFlowScanState.tryEmit(State.Error)
            }
        }

        return false
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (state == State.Scanning) {
            Log.d(logTag, "stopScan()")
            bleManager.bluetoothLeScanner.stopScan(bleScanPendingIntent)
            mutableStateFlowScanState.tryEmit(State.Stopped)
        }
    }


    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(logTag, "onCreate()")
        bleManager.applicationContext.registerReceiver(bcScanReceiver, makeIntentFilters())
    }

    override fun onDestroy(owner: LifecycleOwner) {
        bleManager.applicationContext.unregisterReceiver(bcScanReceiver)
        stopScan()
        super.onDestroy(owner)
    }

    private fun initScanFilters() {
        val filter = ScanFilter.Builder().build()
        scanFilters.add(filter)
    }

    @SuppressLint("MissingPermission")
    private fun filterName(bluetoothDevice: BluetoothDevice) : Boolean =
        names.isEmpty()
            .or(names.isNotEmpty()
                .and(bluetoothDevice.name != null)
                .and(names.contains(bluetoothDevice.name)))

    private fun filterAddress(bluetoothDevice: BluetoothDevice) : Boolean =
        addresses.isEmpty()
            .or(addresses.isNotEmpty().and(addresses.contains(bluetoothDevice.address)))

    private fun filterUuids(uuids: Array<ParcelUuid>?) : Boolean {
        if (this.uuids.isEmpty()) return true
        // println("UUIDS: ${this.uuids}")
        if (uuids.isNullOrEmpty()) return false
        if (this.uuids.containsAll(uuids.toList())) return true
        return false
    }

    private fun isNewDevice(scanResult: ScanResult) : Boolean {
        scanResult.device.let { bluetoothDevice ->
            if (!devices.contains(bluetoothDevice)) {
                scanResults.add(scanResult)
                return  true
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun filterScanResult (scanResult: ScanResult) {
        scanResult.device.let { bluetoothDevice ->
            if (filterName(bluetoothDevice)
                    .and(filterAddress(bluetoothDevice))
                    .and(filterUuids(bluetoothDevice.uuids))
            ) {
                mutableSharedFlowScanResult.tryEmit(scanResult)

                if (stopOnFind &&
                    (names.isNotEmpty()
                        .or(addresses.isNotEmpty()
                            .or(uuids.isNotEmpty())))) {
                    stopScan()
                }
            }
        }
    }

    fun onReceiveError(errorCode: Int) {
        mutableFlowStateError.tryEmit(errorCode)
        stopScan()
    }

    fun onReceiveScanResult(scanResult: ScanResult) {
        val contains = scanResults.map { it.device }.contains(scanResult.device)
        Log.d(logTag, "ScanResult: ${scanResult.device}")
        if (!contains) {
            scanResults.add(scanResult)
        }

        if (!notEmitRepeat || !contains) {
            filterScanResult(scanResult)
        }
    }

    private fun makeIntentFilters() : IntentFilter = IntentFilter().let { intentFilter ->
        intentFilter.addAction(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction(BcScanReceiver.ACTION_BLE_SCAN)
        intentFilter
    }

    private fun initScanSettings() {
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        // setReportDelay() -- отсутствует. Не вызывать! Ответ приходит ПУСТОЙ!
        // В официальной документации scanSettingsBuilder.setReportDelay(1000)
        scanSettingsBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        scanSettingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        scanSettingsBuilder.setLegacy(false)
        scanSettingsBuilder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
    }
}