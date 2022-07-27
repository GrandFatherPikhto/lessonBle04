package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.data.BleGatt
import com.grandfatherpikhto.blin.data.BleService
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener
import kotlin.properties.Delegates

class RvServicesAdapter : RecyclerView.Adapter<RvServiceHolder>() {

    var bleGatt: BleGatt? by Delegates.observable(null) { _, oldValue, newValue ->
        if (newValue == null) {
            notifyItemRangeRemoved(0, oldValue?.services?.size ?: 0)
        } else {
            notifyItemRangeInserted(0, newValue.services.size)
        }
    }

    private var handlerClick: OnClickItemListener<BleService>? = null
    private var handlerLongClick: OnLongClickItemListener<BleService>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvServiceHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_service, parent, false)

        return RvServiceHolder(view)
    }

    override fun onBindViewHolder(holder: RvServiceHolder, position: Int) {
        bleGatt?.let { gatt ->
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

    override fun getItemCount(): Int = bleGatt?.services?.size ?: 0
}