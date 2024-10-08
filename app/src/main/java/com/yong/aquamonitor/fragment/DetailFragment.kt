package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.adapter.DetailDataRecyclerAdapter
import com.yong.aquamonitor.util.AquaMonitorData
import com.yong.aquamonitor.util.DrinkType
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DetailFragment: Fragment(), DetailDataRecyclerAdapter.OnItemClickListener {
    private var recyclerDetailData: RecyclerView? = null

    private var detailDataList = mutableListOf<AquaMonitorData>()
    private var recyclerDetailAdapter: DetailDataRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_detail, container, false)

        recyclerDetailAdapter = DetailDataRecyclerAdapter(detailDataList, this, this)
        recyclerDetailData = layoutInflater.findViewById(R.id.main_detail_recycler)

        recyclerDetailData!!.adapter = recyclerDetailAdapter
        recyclerDetailData!!.layoutManager = LinearLayoutManager(requireActivity())

        getDataList()

        return layoutInflater
    }

    private fun getDataList() {
        lifecycleScope.launch {
            val dataList = HealthConnectUtil.getTotalHydrationData(requireActivity())
            detailDataList.clear()
            dataList.forEach { data ->
                if(detailDataList.isEmpty() ||
                    SimpleDateFormat("DD", Locale.getDefault()).format(data.timeFrom)
                        != SimpleDateFormat("DD", Locale.getDefault()).format(detailDataList.last().timeFrom)) {
                    detailDataList.add(AquaMonitorData(-1, -1.0, data.timeFrom, data.timeTo, null, null))
                }
                detailDataList.add(data)
                recyclerDetailAdapter!!.notifyItemInserted(detailDataList.size)
            }
        }
    }

    override fun onItemDeleteClick(position: Int, dataItem: AquaMonitorData) {
        lifecycleScope.launch {
            HealthConnectUtil.deleteHydration(dataItem.id ?: "", requireActivity())
            detailDataList.removeAt(position)
            recyclerDetailAdapter!!.notifyItemRemoved(position)
        }
    }

    override fun onItemEditClick(position: Int, dataItem: AquaMonitorData) {
        AlertDialog.Builder(requireContext())
            .setTitle("종류 선택")
            .setItems(arrayOf("물", "커피", "음료")) { _, select ->
                when(select) {
                    0 -> dataItem.type = DrinkType.DRINK_WATER
                    1 -> dataItem.type = DrinkType.DRINK_COFFEE
                    2 -> dataItem.type = DrinkType.DRINK_BEVERAGE
                }

                PreferenceUtil.saveHealthData(dataItem, requireActivity())
                recyclerDetailAdapter!!.notifyItemChanged(position)
            }
            .show()
    }
}