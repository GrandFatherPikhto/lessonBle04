package com.grandfatherpikhto.lessonble04.models

import androidx.lifecycle.ViewModel
import com.grandfatherpikhto.blin.BleManager

class ScanViewModel(private val bleManager: BleManager) : ViewModel() {
    val sharedFlowScanResult get() = bleManager.sharedFlowScanResult

    val stateFLowScanState   get() = bleManager.stateFlowScanState
    val scanState            get() = bleManager.scanState

    val stateFlowError       get() = bleManager.stateFlowScanError
    val scanError            get() = bleManager.scanError
}