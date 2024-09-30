package com.yong.aquamonitor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.util.AquaMonitorData
import com.yong.aquamonitor.util.DrinkType
import java.text.SimpleDateFormat
import java.util.Locale

class DetailDataRecyclerAdapter(
    private val dataList: List<AquaMonitorData>,
    private val itemDeleteClickListener: OnItemClickListener,
    private val itemEditClickListener: OnItemClickListener
): RecyclerView.Adapter<DetailDataRecyclerAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemDeleteClick(position: Int, dataItem: AquaMonitorData)
        fun onItemEditClick(position: Int, dataItem: AquaMonitorData)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icDelete: ImageButton = itemView.findViewById(R.id.recycler_item_detail_delete)
        val icEdit: ImageButton = itemView.findViewById(R.id.recycler_item_detail_edit)
        val icType: ImageView = itemView.findViewById(R.id.recycler_item_detail_icon)
        val tvTimeFrom: TextView = itemView.findViewById(R.id.recycler_item_detail_time_from)
        val tvTimeTo: TextView = itemView.findViewById(R.id.recycler_item_detail_time_to)
        val tvValue: TextView = itemView.findViewById(R.id.recycler_item_detail_value)

        init {
            icDelete.setOnClickListener { view ->
                itemDeleteClickListener.onItemDeleteClick(adapterPosition, dataList[adapterPosition])
            }
            icEdit.setOnClickListener { view ->
                itemEditClickListener.onItemEditClick(adapterPosition, dataList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_detail_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.tvValue.text = dataItem.value.toString()
        holder.tvTimeFrom.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dataItem.timeFrom)
        holder.tvTimeTo.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dataItem.timeTo)

        when(dataItem.type) {
            DrinkType.DRINK_BEVERAGE -> holder.icType.setImageResource(R.drawable.circle_beverage)
            DrinkType.DRINK_COFFEE -> holder.icType.setImageResource(R.drawable.circle_coffee)
            DrinkType.DRINK_WATER -> holder.icType.setImageResource(R.drawable.circle_water)
            else -> holder.icType.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = dataList.size
}