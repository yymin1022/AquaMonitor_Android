package com.yong.aquamonitor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.util.AquaMonitorData

class DetailDataRecyclerAdapter(
    private val dataList: List<AquaMonitorData>,
    private val itemClickListener: OnItemClickListener
): RecyclerView.Adapter<DetailDataRecyclerAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(dataItem: AquaMonitorData)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icType: ImageView = itemView.findViewById(R.id.recycler_item_detail_icon)
        val tvTimeFrom: TextView = itemView.findViewById(R.id.recycler_item_detail_time_from)
        val tvTimeTo: TextView = itemView.findViewById(R.id.recycler_item_detail_time_to)
        val tvValue: TextView = itemView.findViewById(R.id.recycler_item_detail_value)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(dataList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_ble_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.tvValue.text = dataItem.value.toString()
        holder.tvTimeFrom.text = dataItem.timeFrom.toString()
        holder.tvTimeTo.text = dataItem.timeTo.toString()
    }

    override fun getItemCount() = dataList.size
}