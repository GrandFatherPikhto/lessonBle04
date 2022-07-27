package com.grandfatherpikhto.lessonble04.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.grandfatherpikhto.blin.BleManagerInterface
import com.grandfatherpikhto.blin.BleScanManager
import com.grandfatherpikhto.lessonble04.LessonBle04App
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.databinding.FragmentScanBinding
import com.grandfatherpikhto.lessonble04.helper.linkMenu
import com.grandfatherpikhto.lessonble04.models.MainActivityViewModel
import com.grandfatherpikhto.lessonble04.models.ScanViewModel
import com.grandfatherpikhto.lessonble04.ui.adapters.RvBtAdapter
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ScanFragment : Fragment() {

    private val logTag = this.javaClass.simpleName
    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val scanViewModel by viewModels<ScanViewModel>()

    private val _bleManager:BleManagerInterface? by lazy {
        (requireActivity().application as LessonBle04App).bleManager
    }
    private val bleManager get() = _bleManager!!

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_scan, menu)
            menu.findItem(R.id.action_scan).let { actionScan ->
                lifecycleScope.launch {
                    scanViewModel.stateFlowScanState.collect { state ->
                        Log.d(logTag, "New State: $state")
                        when(state) {
                            BleScanManager.State.Stopped -> {
                                actionScan.setIcon(R.drawable.ic_scan)
                                actionScan.title = getString(R.string.scan_start)
                            }
                            BleScanManager.State.Scanning -> {
                                actionScan.setIcon(R.drawable.ic_stop)
                                actionScan.title = getString(R.string.scan_start)
                                Log.d(logTag, getString(R.string.scan_stop))
                            }
                            BleScanManager.State.Error -> {
                                actionScan.setIcon(R.drawable.ic_error)
                                actionScan.title = getString(R.string.scan_error, scanViewModel.scanError)
                            }
                        }
                    }
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.action_scan -> {
                    when(scanViewModel.scanState) {
                        BleScanManager.State.Scanning -> {
                            bleManager.stopScan()
                        }
                        BleScanManager.State.Stopped -> {
                            bleManager.startScan(stopTimeout = 15000L)
                        }
                        BleScanManager.State.Error -> {
                            bleManager.stopScan()
                        }
                    }
                   true
                }
                else -> { false }
            }
        }
    }

    private val rvBtAdapter = RvBtAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        scanViewModel.changeBleManager(bleManager)
        linkMenu(true, menuProvider)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            rvBleDevices.adapter = rvBtAdapter
            rvBleDevices.layoutManager = LinearLayoutManager(requireContext())
        }

        rvBtAdapter.setItemOnClickListener { scanResult, _ ->
            if (scanResult.isConnectable) {
                mainActivityViewModel.changeScanResult(scanResult)
                findNavController().navigate(R.id.action_ScanFragment_to_DeviceFragment)
            }
        }

        lifecycleScope.launch {
            scanViewModel.sharedFlowScanResult.collect { scanResult ->
                rvBtAdapter.addScanResult(scanResult)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        linkMenu(false, menuProvider)
        bleManager.stopScan()
        _binding = null
    }
}