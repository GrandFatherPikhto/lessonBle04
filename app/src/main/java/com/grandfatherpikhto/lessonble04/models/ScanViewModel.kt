package com.grandfatherpikhto.lessonble04.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.BleManagerInterface
import com.grandfatherpikhto.blin.BleScanManager
import com.grandfatherpikhto.blin.data.BleScanResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScanViewModel() : ViewModel() {
    private val scannedResults = mutableListOf<BleScanResult>()

    private val mutableSharedFlowScanResult = MutableSharedFlow<BleScanResult>(replay = 100)
    private val mutableStateFLowScanState   = MutableStateFlow(BleScanManager.State.Stopped)
    private val mutableStateFlowScanError   = MutableStateFlow(-1)

    val sharedFlowScanResult  get() = mutableSharedFlowScanResult.asSharedFlow()
    val stateFlowScanState    get() = mutableStateFLowScanState.asStateFlow()
    val scanState             get() = mutableStateFLowScanState.value
    val stateFlowScanError    get() = mutableStateFlowScanError.asStateFlow()
    val scanError             get() = mutableStateFlowScanError.value

    fun changeBleManager(bleManager: BleManagerInterface) {
        viewModelScope.launch {
            bleManager.sharedFlowScanResult.collect {
                mutableSharedFlowScanResult.tryEmit(it)
            }
        }

        viewModelScope.launch {
            bleManager.stateFlowScanState.collect {
                mutableStateFLowScanState.tryEmit(it)
            }
        }

        viewModelScope.launch {
            bleManager.stateFlowScanError.collect {
                mutableStateFlowScanError.tryEmit(it)
            }
        }
    }
}