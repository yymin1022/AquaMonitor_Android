package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.adapter.AlarmRecyclerAdapter
import com.yong.aquamonitor.adapter.DetailDataRecyclerAdapter
import com.yong.aquamonitor.util.AlarmData
import com.yong.aquamonitor.util.AquaMonitorData
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.Logger
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmFragment: Fragment(), AlarmRecyclerAdapter.OnItemClickListener {
    private var btnAdd: Button? = null
    private var recyclerAlarmList: RecyclerView? = null

    private var alarmDataList = mutableListOf<AlarmData>()
    private var recyclerAlarmAdapter: AlarmRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_alarm, container, false)

        recyclerAlarmAdapter = AlarmRecyclerAdapter(alarmDataList, this)
        recyclerAlarmList = layoutInflater.findViewById(R.id.main_alarm_recycler)
        recyclerAlarmList!!.adapter = recyclerAlarmAdapter
        recyclerAlarmList!!.layoutManager = LinearLayoutManager(requireActivity())

        btnAdd = layoutInflater.findViewById(R.id.main_alarm_btn_add)
        btnAdd!!.setOnClickListener(btnListener)

        getDataList()

        return layoutInflater
    }

    private fun getDataList() {
        val dataList = PreferenceUtil.getAlarmList(requireActivity())
        alarmDataList.clear()
        dataList.forEach { alarm ->
            alarmDataList.add(alarm)
            Logger.LogI(alarm.value.toString())
            recyclerAlarmAdapter!!.notifyItemInserted(alarmDataList.size)
        }
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.main_alarm_btn_add -> {
                
            }
        }
    }

    override fun onItemDeleteClick(position: Int) {
        TODO("Not yet implemented")
    }
}