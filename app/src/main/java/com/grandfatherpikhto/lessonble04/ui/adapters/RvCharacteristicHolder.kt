package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGattCharacteristic
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.data.BleCharacteristic
import com.grandfatherpikhto.lessonble04.databinding.LayoutCharacteristicBinding

@OptIn(ExperimentalUnsignedTypes::class)
class RvCharacteristicHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val logTag = this.javaClass.simpleName
    private val binding = LayoutCharacteristicBinding.bind(view)

    fun bind(bleCharacteristic: BleCharacteristic) {
        binding.apply {
            tvCharacteristic.text = bleCharacteristic.uuid.toString()
            val value = bleCharacteristic.value.toUByteArray()
                .joinToString(", ") { String.format("%02X", it.toByte()) }
            tvValue.text = value
        }
    }
}