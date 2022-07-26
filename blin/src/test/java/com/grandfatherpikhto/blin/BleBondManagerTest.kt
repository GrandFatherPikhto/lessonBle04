package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.lenient
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BleBondManagerTest {
    companion object {
        const val ADDRESS = "01:02:03:04:05"
        const val NAME    = "BLE_DEVICE"
    }

    private lateinit var closeable:AutoCloseable
    private val bleManager =
        BleManager(
            ApplicationProvider.getApplicationContext<Context?>().applicationContext,
            UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun bondDevice() = runTest(UnconfinedTestDispatcher()) {
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS, name = NAME)
        lenient().`when`(bluetoothDevice.bondState).thenReturn(BluetoothDevice.BOND_NONE)
        lenient().`when`(bluetoothDevice.createBond()).thenReturn(true)
        assertEquals(BleBondManager.State.None, bleManager.stateBond)
        bleManager.bondRequest(bluetoothDevice)
        assertEquals(BleBondManager.State.Bonding, bleManager.stateBond)
        lenient().`when`(bluetoothDevice.bondState).thenReturn(BluetoothDevice.BOND_BONDED)
        bleManager.bleBondManager.onSetBondingDevice(bluetoothDevice, BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_BONDED)
        assertEquals(BleBondManager.State.Bondend, bleManager.stateBond)
    }

    @Test
    fun errorBondingDevice() = runTest(UnconfinedTestDispatcher()) {
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS, name = NAME)
        lenient().`when`(bluetoothDevice.bondState).thenReturn(BluetoothDevice.BOND_NONE)
        lenient().`when`(bluetoothDevice.createBond()).thenReturn(false)
        assertEquals(BleBondManager.State.None, bleManager.stateBond)
        bleManager.bleBondManager.bondRequest(bluetoothDevice)
        assertEquals(BleBondManager.State.Error, bleManager.stateBond)
    }

    @Test
    fun rejectBondDevice() = runTest(UnconfinedTestDispatcher()) {
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS, name = NAME)
        lenient().`when`(bluetoothDevice.bondState).thenReturn(BluetoothDevice.BOND_NONE)
        lenient().`when`(bluetoothDevice.createBond()).thenReturn(true)
        assertEquals(BleBondManager.State.None, bleManager.stateBond)
        bleManager.bleBondManager.bondRequest(bluetoothDevice)
        assertEquals(BleBondManager.State.Bonding, bleManager.stateBond)
        lenient().`when`(bluetoothDevice.bondState).thenReturn(BluetoothDevice.BOND_BONDED)
        bleManager.bleBondManager.onSetBondingDevice(bluetoothDevice, BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_NONE)
        assertEquals(BleBondManager.State.Reject, bleManager.stateBond)
    }
}