package com.yong.aquamonitor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R

class BleScanRecyclerAdapter(
    private val devices: List<Pair<String, String>>,
    private val itemClickListener: OnItemClickListener
): RecyclerView.Adapter<BleScanRecyclerAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(device: Pair<String, String>)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.recycler_item_device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.recycler_item_device_address)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(devices[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_ble_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.first
        holder.deviceAddress.text = device.second
    }

    override fun getItemCount() = devices.size
}