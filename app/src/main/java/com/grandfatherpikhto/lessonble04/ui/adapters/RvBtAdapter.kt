package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.blin.data.BleScanResult
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener

class RvBtAdapter : RecyclerView.Adapter<RvBtHolder> () {
    private val scanResults = mutableListOf<BleScanResult>()
    private var onClickItemListener: OnClickItemListener<BleScanResult>? = null
    private var onLongClickItemListener: OnLongClickItemListener<BleScanResult>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvBtHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_ble_device, parent, false)
        return RvBtHolder(view)
    }

    override fun onBindViewHolder(holder: RvBtHolder, position: Int) {
        holder.itemView.setOnClickListener { view ->
            onClickItemListener?.let { listener ->
                listener(scanResults[position], view)
            }
        }
        holder.itemView.setOnLongClickListener { view ->
            onLongClickItemListener?.let { listener ->
                listener(scanResults[position], view)
            }
            true
        }
        holder.bind(scanResults[position])
    }

    override fun getItemCount(): Int = scanResults.size

    fun addScanResult(bleScanResult: BleScanResult) {
        if (!scanResults.map { it.device }.contains(bleScanResult.device)) {
            scanResults.add(bleScanResult)
            notifyItemInserted(scanResults.indexOf(bleScanResult))
        }
    }

    fun setItemOnClickListener(onClickItemListener: OnClickItemListener<BleScanResult>) {
        this.onClickItemListener = onClickItemListener
    }

    fun setItemOnLongCliclListener(onLongClickItemListener: OnLongClickItemListener<BleScanResult>) {
        this.onLongClickItemListener = onLongClickItemListener
    }
}