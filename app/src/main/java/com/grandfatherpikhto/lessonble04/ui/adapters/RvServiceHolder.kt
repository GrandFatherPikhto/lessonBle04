package com.grandfatherpikhto.lessonble04.ui.adapters

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.data.BleService
import com.grandfatherpikhto.lessonble04.databinding.LayoutServiceBinding

class RvServiceHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = LayoutServiceBinding.bind(view)
    private val rvCharacteristicsAdapter = RvCharacteristicsAdapter()

    fun bind(bleService: BleService) {
        binding.apply {
            tvService.text = bleService.uuid.toString()
            rvCharacteristics.adapter = rvCharacteristicsAdapter
            rvCharacteristics.layoutManager = LinearLayoutManager(view.context)
            rvCharacteristicsAdapter.bleService = bleService
        }
    }
}