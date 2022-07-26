package com.grandfatherpikhto.lessonble04.ui.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.grandfatherpikhto.blin.*
import com.grandfatherpikhto.lessonble04.LessonBle04App
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.databinding.FragmentDeviceBinding
import com.grandfatherpikhto.lessonble04.helper.linkMenu
import com.grandfatherpikhto.lessonble04.models.*
import com.grandfatherpikhto.lessonble04.ui.adapters.RvBtAdapter
import com.grandfatherpikhto.lessonble04.ui.adapters.RvServicesAdapter
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DeviceFragment : Fragment() {

    private val logTag = this.javaClass.simpleName
    private var _binding: FragmentDeviceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val deviceViewModel by viewModels<DeviceViewModel> {
        BleDeviceViewModelProviderFactory(requireActivity().application)
    }

    private val _bleManager: BleManagerInterface? by lazy {
        (requireActivity().application as LessonBle04App).bleManager
    }
    private val bleManager get() = _bleManager!!

    private val rvServicesAdapter = RvServicesAdapter()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_device, menu)
            menu.findItem(R.id.action_connect)?.let { actionConnect ->
                lifecycleScope.launch {
                    deviceViewModel.stateFlowConnectState.collect { state ->
                        when(state) {
                            BleGattManager.State.Disconnected -> {
                                actionConnect.setIcon(R.drawable.ic_connect)
                                actionConnect.title = getString(R.string.device_connect)
                            }
                            BleGattManager.State.Connected -> {
                                actionConnect.setIcon(R.drawable.ic_disconnect)
                                actionConnect.title = getString(R.string.device_disconnect)
                            }
                            else -> { }
                        }
                    }
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.action_connect -> {
                    when(deviceViewModel.connectState) {
                        BleGattManager.State.Connected -> {
                            bleManager.disconnect()
                        }
                        BleGattManager.State.Disconnected -> {
                            mainActivityViewModel.scanResult?.let { scanResult ->
                                Log.d(logTag, "Try Connecting(${scanResult.device.address})")
                                bleManager.connect(scanResult.device.address)
                            }
                        }
                        else -> {

                        }
                    }
                    true
                }
                else -> { false }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linkMenu(true, menuProvider)
        binding.apply {
            rvServices.adapter = rvServicesAdapter
            rvServices.layoutManager = LinearLayoutManager(requireContext())
        }

        lifecycleScope.launch {
            deviceViewModel.stateFlowGatt.collect { bluetoothGatt ->
                rvServicesAdapter.bluetoothGatt = bluetoothGatt
            }
        }

        mainActivityViewModel.scanResult?.let { scanResult ->
            binding.apply {
                tvBleAddress.text = scanResult.device.address
                tvBleName.text = scanResult.device.name ?: getString(R.string.unknown_device)
                tvBleRssi.text = getString(R.string.rssi_title, scanResult.rssi)
                Log.d(logTag, "bondState = ${scanResult.device.bondState} {${BluetoothDevice.BOND_BONDED}}")
                if (scanResult.device.bondState == BluetoothDevice.BOND_BONDED) {
                    ivBlePaired.setImageResource(R.drawable.ic_paired)
                } else {
                    ivBlePaired.setImageResource(R.drawable.ic_unpaired)
                }
            }
        }

        lifecycleScope.launch {
            deviceViewModel.stateFlowConnectState.collect { state ->
                when(state) {
                    BleGattManager.State.Disconnected -> {
                        binding.ivBleConnected.setImageResource(R.drawable.ic_connect_big)
                    }
                    BleGattManager.State.Connected -> {
                        binding.ivBleConnected.setImageResource(R.drawable.ic_disconnect_big)
                    }
                    else -> { }
                }
            }
        }

        binding.ivBleConnected.setOnClickListener { _ ->
            when(deviceViewModel.connectState) {
                BleGattManager.State.Connected -> {
                    bleManager.disconnect()
                }
                BleGattManager.State.Disconnected -> {
                    mainActivityViewModel.scanResult?.let { scanResult ->
                        bleManager.connect(scanResult.device.address)
                    }
                }
                else -> { }
            }
        }

        binding.ivBlePaired.setOnClickListener { _ ->
            mainActivityViewModel.scanResult?.let { scanResult ->
                if (scanResult.device.bondState != BluetoothDevice.BOND_BONDED) {
                    bleManager.bondRequest(scanResult.device)
                }
            }
        }

        lifecycleScope.launch {
            deviceViewModel.stateFlowBondState.collect { bondState ->
                if (bondState == BleBondManager.State.Bondend) {
                    binding.ivBlePaired.setImageResource(R.drawable.ic_paired)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bleManager.disconnect()
        linkMenu(false, menuProvider)
        _binding = null
    }
}