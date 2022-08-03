package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.data.BleGatt
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener
import kotlin.properties.Delegates

class RvServicesAdapter : RecyclerView.Adapter<RvServiceHolder>() {

    private val services = mutableListOf<BluetoothGattService>()

    var bleGatt: BleGatt? by Delegates.observable(null) { _, _, newValue ->
        if (newValue == null) {
            clear()
        } else {
            setItems(bleGatt!!.services)
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
        holder.itemView.setOnClickListener { view ->
            handlerClick?.let { it(services[position], view) }
        }

        holder.itemView.setOnLongClickListener { view ->
            handlerLongClick?.let { it(services[position], view) }
            true
        }

        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size

    fun clear() {
        val size = services.size
        services.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun setItems(services: List<BluetoothGattService>) {
        clear()
        this.services.addAll(services)
        notifyItemRangeInserted (0, this.services.size)
    }

    fun addItem(service: BluetoothGattService) {
        services.add(service)
        notifyItemInserted(services.size - 1)
    }
}