package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R

class AlarmFragment : Fragment() {
    private var btnAdd: Button? = null
    private var recyclerAlarmList: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_alarm, container, false)
        recyclerAlarmList = layoutInflater.findViewById(R.id.main_alarm_recycler)
        btnAdd = layoutInflater.findViewById(R.id.main_alarm_btn_add)
        btnAdd!!.setOnClickListener(btnListener)

        return layoutInflater
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.main_alarm_btn_add -> {

            }
        }
    }
}