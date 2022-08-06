package com.grandfatherpikhto.lessonble04.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grandfatherpikhto.blin.BleBondManager
import com.grandfatherpikhto.blin.BleGattManager
import com.grandfatherpikhto.blin.BleManagerInterface
import com.grandfatherpikhto.blin.data.BleBondState
import com.grandfatherpikhto.blin.data.BleGatt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceViewModel: ViewModel () {

    private val logTag = this.javaClass.simpleName

    private val mutableStateFlowConnectState = MutableStateFlow(BleGattManager.State.Disconnected)
    val stateFlowConnectState get() = mutableStateFlowConnectState.asStateFlow()
    val connectState          get() = mutableStateFlowConnectState.value

    private val mutableStateFlowStateCode = MutableStateFlow(-1)
    val stateFlowConnectStateCode get() = mutableStateFlowStateCode.asStateFlow()
    val stateCode                 get() = mutableStateFlowStateCode.value

    private val mutableStateFlowBleGatt = MutableStateFlow<BleGatt?>(null)
    val stateFlowGatt get() = mutableStateFlowBleGatt.asStateFlow()
    val bluetoothGatt get() = mutableStateFlowBleGatt.value

    private val mutableStateFlowBond = MutableStateFlow<BleBondState?>(null)
    val stateFlowBondState get() = mutableStateFlowBond.asStateFlow()
    val bondState          get() = mutableStateFlowBond.value

    var connected = false

    fun changeBleManager(bleManager: BleManagerInterface) {
        viewModelScope.launch {
            bleManager.stateFlowConnectState.collect {
                mutableStateFlowConnectState.tryEmit(it)
            }
        }

        viewModelScope.launch {
            bleManager.sharedFlowConnectStateCode.collect {
                mutableStateFlowStateCode.tryEmit(it)
            }
        }

        viewModelScope.launch {
            bleManager.stateFlowGatt.collect {
                mutableStateFlowBleGatt.tryEmit(it)
            }
        }

        viewModelScope.launch {
            bleManager.stateFlowBondState.collect {
                mutableStateFlowBond.tryEmit(it)
            }
        }
    }

    override fun onCleared() {
        Log.d(logTag, "onCleared()")
        super.onCleared()
    }
}