package com.grandfatherpikhto.blin

import androidx.lifecycle.DefaultLifecycleObserver
import com.grandfatherpikhto.blin.data.BleGatt
import com.grandfatherpikhto.blin.data.BleScanResult
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleManagerInterface : DefaultLifecycleObserver {
    val stateFlowScanState:StateFlow<BleScanManager.State>
    val scanState:BleScanManager.State

    val sharedFlowScanResult: SharedFlow<BleScanResult>
    val scanResults:List<BleScanResult>

    val stateFlowScanError: StateFlow<Int>
    val scanError: Int

    val stateFlowConnectState: StateFlow<BleGattManager.State>
    val connectState: BleGattManager.State

    val sharedFlowConnectStateCode: SharedFlow<Int>

    val stateFlowGatt: StateFlow<BleGatt?>
    val bluetoothGatt: BleGatt?

    val stateFlowBondState: StateFlow<BleBondManager.State>
    val stateBond: BleBondManager.State

    fun bondRequest(address: String): Boolean

    fun startScan(addresses: List<String> = listOf(),
                  names: List<String> = listOf(),
                  services: List<String> = listOf(),
                  stopOnFind: Boolean = false,
                  filterRepeatable: Boolean = true,
                  stopTimeout: Long = 0L
    ) : Boolean

    fun stopScan()

    fun connect(address: String) : BleGatt?
    fun disconnect()
}