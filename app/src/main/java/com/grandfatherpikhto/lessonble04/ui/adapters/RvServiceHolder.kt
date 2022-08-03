package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGattService
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble04.databinding.LayoutServiceBinding

class RvServiceHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = LayoutServiceBinding.bind(view)
    private val rvCharacteristicsAdapter = RvCharacteristicsAdapter()

    fun bind(service: BluetoothGattService) {
        binding.apply {
            tvService.text = service.uuid.toString()
            rvCharacteristics.adapter = rvCharacteristicsAdapter
            rvCharacteristics.layoutManager = LinearLayoutManager(view.context)
            rvCharacteristicsAdapter.bleService = service
        }
    }
}