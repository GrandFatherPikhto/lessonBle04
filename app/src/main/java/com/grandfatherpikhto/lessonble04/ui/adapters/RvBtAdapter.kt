package com.grandfatherpikhto.lessonble04.ui.adapters

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.OnClickItemListener
import com.grandfatherpikhto.lessonble04.helper.OnLongClickItemListener

class RvBtAdapter : RecyclerView.Adapter<RvBtHolder> () {
    private val scanResults = mutableListOf<ScanResult>()
    private var onClickItemListener: OnClickItemListener<ScanResult>? = null
    private var onLongClickItemListener: OnLongClickItemListener<ScanResult>? = null

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

    fun addScanResult(scanResult: ScanResult) {
        if (!scanResults.map { it.device }.contains(scanResult.device)) {
            scanResults.add(scanResult)
            notifyItemInserted(scanResults.indexOf(scanResult))
        }
    }

    fun setItemOnClickListener(onClickItemListener: OnClickItemListener<ScanResult>) {
        this.onClickItemListener = onClickItemListener
    }

    fun setItemOnLongCliclListener(onLongClickItemListener: OnLongClickItemListener<ScanResult>) {
        this.onLongClickItemListener = onLongClickItemListener
    }
}