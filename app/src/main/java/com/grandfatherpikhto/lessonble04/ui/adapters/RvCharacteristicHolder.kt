package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGattCharacteristic
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.GenericUUIDs.findGeneric
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.databinding.LayoutCharacteristicBinding

@OptIn(ExperimentalUnsignedTypes::class)
class RvCharacteristicHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val logTag = this.javaClass.simpleName
    private val binding = LayoutCharacteristicBinding.bind(view)

    fun bind(characteristic: BluetoothGattCharacteristic) {
        binding.apply {
            tvCharacteristicName.text =
                characteristic.uuid.findGeneric()?.let{ uuid16 ->
                    uuid16.name
                } ?: view.context.getString(R.string.custom_charactersistic)
            tvCharacteristicUuid.text =
                characteristic.uuid.findGeneric()?.let { uuiD16 ->
                    String.format("%04X", uuiD16.uuid)
                } ?: characteristic.uuid.toString()
        }
    }
}