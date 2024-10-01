package com.yong.aquamonitor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.util.AlarmData
import java.util.Locale

class AlarmRecyclerAdapter(
    private val dataList: List<AlarmData>,
    private val itemDeleteClickListener: OnItemClickListener
): RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemDeleteClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icDelete: ImageButton = itemView.findViewById(R.id.recycler_item_alarm_delete)
        val tvTime: TextView = itemView.findViewById(R.id.recycler_item_alarm_time)
        val tvValue: TextView = itemView.findViewById(R.id.recycler_item_alarm_value)

        init {
            icDelete.setOnClickListener { view ->
                itemDeleteClickListener.onItemDeleteClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_alarm_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", dataItem.hour, dataItem.min)
        holder.tvValue.text = String.format(Locale.getDefault(), "%dml", dataItem.value)
    }

    override fun getItemCount() = dataList.size
}