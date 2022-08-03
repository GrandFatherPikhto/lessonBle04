package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener
import kotlin.properties.Delegates

class RvCharacteristicsAdapter : RecyclerView.Adapter<RvCharacteristicHolder> () {
    private val logTag = this.javaClass.simpleName
    private val characteristics = mutableListOf<BluetoothGattCharacteristic>()

    var bleService: BluetoothGattService?
            by Delegates.observable(null) { _, _, newValue ->
                if (newValue == null) {
                    clear()
                } else {
                    setItems(newValue.characteristics)
                }
            }

    private var handlerClick: OnClickItemListener<BluetoothGattCharacteristic>? = null
    private var handlerLongClick: OnLongClickItemListener<BluetoothGattCharacteristic>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvCharacteristicHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_characteristic, parent, false)
        return RvCharacteristicHolder(view)
    }

    override fun onBindViewHolder(holder: RvCharacteristicHolder, position: Int) {
        bleService?.let { service ->
            service.characteristics.let { characteristics ->
                holder.itemView.setOnClickListener { view ->
                    handlerClick?.let { it(characteristics[position], view) }
                }

                holder.itemView.setOnLongClickListener { view ->
                    handlerLongClick?.let { it(characteristics[position], view) }
                    true
                }

                holder.bind(characteristics[position])
            }
        }
    }

    override fun getItemCount(): Int = bleService?.characteristics?.size ?: 0

    fun clear() {
        val size = characteristics.size
        characteristics.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun setItems(characteristics: List<BluetoothGattCharacteristic>) {
        clear()
        this.characteristics.addAll(characteristics)
        notifyItemRangeInserted(0, this.characteristics.size)
    }

    fun addItem(characteristic: BluetoothGattCharacteristic) {
        characteristics.add(characteristic)
        notifyItemInserted(characteristics.size - 1)
    }
}
