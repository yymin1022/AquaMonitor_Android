package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yong.aquamonitor.R

class HomeFragment: Fragment() {
    private var tvAlarmTime: TextView? = null
    private var tvAlarmValue: TextView? = null
    private var tvTargetRemain: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_home, container, false)
        tvAlarmTime = layoutInflater.findViewById(R.id.main_home_alarm_time)
        tvAlarmValue = layoutInflater.findViewById(R.id.main_home_alarm_value)
        tvTargetRemain = layoutInflater.findViewById(R.id.main_home_target_remain)

        return layoutInflater
    }
}