package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.adapter.DetailDataRecyclerAdapter
import com.yong.aquamonitor.util.AquaMonitorData

class DetailFragment: Fragment(), DetailDataRecyclerAdapter.OnItemClickListener {
    private var recyclerDetailData: RecyclerView? = null

    private var detailDataList = mutableListOf<AquaMonitorData>()
    private var recyclerDetailAdapter: DetailDataRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_detail, container, false)

        recyclerDetailAdapter = DetailDataRecyclerAdapter(detailDataList, this)
        recyclerDetailData = layoutInflater.findViewById(R.id.main_detail_recycler)

        recyclerDetailData!!.adapter = recyclerDetailAdapter
        recyclerDetailData!!.layoutManager = LinearLayoutManager(requireActivity())

        getDataList()

        return layoutInflater
    }

    private fun getDataList() {

    }

    override fun onItemClick(dataItem: AquaMonitorData) {
        TODO("Not yet implemented")
    }
}