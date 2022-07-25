package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BleGattManagerTest {
    companion object {
        const val ADDRESS="01:02:03:04:05:06"
        const val NAME="BLE_DEVICE"
        const val ERROR_133 = 133
        const val ERROR_6   = 6
    }

    private val bleManager =
        BleManager(
            ApplicationProvider.getApplicationContext<Context?>().applicationContext,
            UnconfinedTestDispatcher()
        )

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun testConnect() {
        bleManager.connect(ADDRESS)
        val gatt = mockBluetoothGatt(ADDRESS)
        bleManager.connector.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
        bleManager.connector.onGattDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)
        assertEquals(BleGattManager.State.Connected, bleManager.connectState)
        assertEquals(gatt, bleManager.bluetoothGatt)
    }

    @Test
    fun testReconnectWithRescan() {
        bleManager.connect(ADDRESS)
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS, name = NAME)
        val bluetoothGatt = mockBluetoothGatt(bluetoothDevice)
        val scanResult = mockScanResult(bluetoothDevice)
        bleManager.connect(bluetoothDevice.address)
        bleManager.connector.onConnectionStateChange(null, ERROR_133, 0)
        assertEquals(BleScanManager.State.Scanning, bleManager.scanState)
        bleManager.scanner.onReceiveScanResult(scanResult)
        assertEquals(BleScanManager.State.Stopped, bleManager.scanState)
        bleManager.connector.onGattDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS)
        assertEquals(BleGattManager.State.Connected, bleManager.connectState)
        assertEquals(bluetoothGatt, bleManager.bluetoothGatt)
    }

    @Test
    fun errorMaxReconnecting() {
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS, name = NAME)
        val scanResult = mockScanResult(bluetoothDevice)
        bleManager.connect(ADDRESS)

        (1..BleGattManager.MAX_ATTEMPTS).forEach { _->
            bleManager.connector.onConnectionStateChange(null, ERROR_133, 0)
            bleManager.scanner.onReceiveScanResult(scanResult)
        }

        assertEquals(BleGattManager.State.Error, bleManager.connectState)
    }
}