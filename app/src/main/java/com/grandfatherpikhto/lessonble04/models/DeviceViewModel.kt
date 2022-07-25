package com.grandfatherpikhto.lessonble04.models

import androidx.lifecycle.ViewModel
import com.grandfatherpikhto.blin.BleManager

class DeviceViewModel (private val bleManager: BleManager): ViewModel () {
    val stateFlowConnectState get() = bleManager.stateFlowConnectState
    val connectState get() = bleManager.connectState

    val stateFlowConnectStateCode get() = bleManager.stateFlowConnectStateCode
    val stateCode get() = bleManager.stateFlowConnectStateCode

    val stateFlowGatt get() = bleManager.stateFlowGatt
    val bluetoothGatt get() = bleManager.bluetoothGatt

    val stateFlowBondState   get() = bleManager.stateFlowBondState
    val stateBonded          get() = bleManager.stateBonded
}