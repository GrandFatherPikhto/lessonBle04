package com.grandfatherpikhto

import android.bluetooth.BluetoothDevice
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

// @RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest  {

    @Test
    fun mockBluetoothDevice() {
        val bluetoothDevice = mock<BluetoothDevice>()
    }
}