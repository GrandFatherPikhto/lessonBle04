package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener
import kotlin.properties.Delegates

class RvServicesAdapter : RecyclerView.Adapter<RvServiceHolder>() {

    var bluetoothGatt: BluetoothGatt? by Delegates.observable(null) { _, oldValue, newValue ->
        if (newValue == null) {
            notifyItemRangeRemoved(0, oldValue?.services?.size ?: 0)
        } else {
            notifyItemRangeInserted(0, newValue?.services?.size ?: 0)
        }
    }

    private var handlerClick: OnClickItemListener<BluetoothGattService>? = null
    private var handlerLongClick: OnLongClickItemListener<BluetoothGattService>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvServiceHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_service, parent, false)

        return RvServiceHolder(view)
    }

    override fun onBindViewHolder(holder: RvServiceHolder, position: Int) {
        bluetoothGatt?.let { gatt ->
            gatt.services?.let { services ->
                holder.itemView.setOnClickListener { view ->
                    handlerClick?.let { it(services[position], view) }
                }

                holder.itemView.setOnLongClickListener { view ->
                    handlerLongClick?.let { it(services[position], view) }
                    true
                }

                holder.bind(services[position])
            }
        }
    }

    override fun getItemCount(): Int = bluetoothGatt?.services?.size ?: 0
}