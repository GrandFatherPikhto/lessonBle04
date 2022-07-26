package com.grandfatherpikhto.lessonble04.models

import androidx.lifecycle.ViewModel
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.BleManagerInterface

class DeviceViewModel (private val bleManager: BleManagerInterface): ViewModel () {
    val stateFlowConnectState get() = bleManager.stateFlowConnectState
    val connectState          get() = bleManager.connectState

    val stateFlowConnectStateCode get() = bleManager.stateFlowConnectStateCode
    val stateCode                 get() = bleManager.stateFlowConnectStateCode

    val stateFlowGatt get() = bleManager.stateFlowGatt
    val bluetoothGatt get() = bleManager.bluetoothGatt

    val stateFlowBondState get() = bleManager.stateFlowBondState
    val stateBond          get() = bleManager.stateBond
}