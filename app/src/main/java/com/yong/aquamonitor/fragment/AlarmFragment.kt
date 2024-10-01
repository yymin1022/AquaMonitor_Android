package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.adapter.AlarmRecyclerAdapter
import com.yong.aquamonitor.util.AlarmData
import com.yong.aquamonitor.util.Logger
import com.yong.aquamonitor.util.PreferenceUtil

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
                val dialogView = layoutInflater.inflate(R.layout.dialog_alarm_add, null)
                val dialogTimeInput = dialogView.findViewById<TimePicker>(R.id.dialog_alarm_add_time)
                val dialogValueInput = dialogView.findViewById<EditText>(R.id.dialog_alarm_add_value)
                dialogTimeInput.setIs24HourView(true)

                AlertDialog.Builder(requireActivity())
                    .setView(dialogView)
                    .setPositiveButton("확인") { _, _ ->
                        val inputHour = dialogTimeInput.hour
                        val inputMinute = dialogTimeInput.minute
                        val inputValue = dialogValueInput.text.toString().toIntOrNull()

                        if(inputValue != null) {
                            alarmDataList.add(AlarmData(inputHour, inputMinute, inputValue))
                            PreferenceUtil.saveAlarmList(alarmDataList, requireActivity())
                            recyclerAlarmAdapter!!.notifyItemInserted(alarmDataList.size)
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    override fun onItemDeleteClick(position: Int) {
        alarmDataList.removeAt(position)
        PreferenceUtil.saveAlarmList(alarmDataList, requireActivity())
        recyclerAlarmAdapter!!.notifyItemRemoved(position)
    }
}