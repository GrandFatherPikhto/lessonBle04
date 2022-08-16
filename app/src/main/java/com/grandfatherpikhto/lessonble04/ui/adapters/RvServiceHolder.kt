package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGattService
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.GenericUUIDs.findGeneric
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.databinding.LayoutServiceBinding

class RvServiceHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = LayoutServiceBinding.bind(view)
    private val rvCharacteristicsAdapter = RvCharacteristicsAdapter()

    fun bind(service: BluetoothGattService) {
        binding.apply {
            tvServiceUuid.text =
                service.uuid.findGeneric()?.let { uuiD16 ->
                    uuiD16.name
                } ?: view.context.getString(R.string.custom_service)
            tvServiceUuid.text =
                service.uuid.findGeneric()?.let{ uuid16 ->
                    String.format("%04X", uuid16.uuid)
                } ?: service.uuid.toString()
            rvCharacteristics.adapter = rvCharacteristicsAdapter
            rvCharacteristics.layoutManager = LinearLayoutManager(view.context)
            rvCharacteristicsAdapter.bleService = service
        }
    }
}