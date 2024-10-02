package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.yong.aquamonitor.R
import com.yong.aquamonitor.activity.MainActivity
import com.yong.aquamonitor.util.DrinkType
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.max

class HomeFragment: Fragment() {
    private var tvAlarmTime: TextView? = null
    private var tvTargetDone: TextView? = null
    private var tvTargetRemain: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_home, container, false)
        tvAlarmTime = layoutInflater.findViewById(R.id.main_home_alarm_time)
        tvTargetDone = layoutInflater.findViewById(R.id.main_home_target_done)
        tvTargetRemain = layoutInflater.findViewById(R.id.main_home_target_remain_value)

        setNextAlarmView()
        setTargetRemainView()

        return layoutInflater
    }

    private fun setNextAlarmView() {
        val curTime = LocalDateTime.now()
        val alarmList = PreferenceUtil.getAlarmList(requireActivity())
        for(alarmData in alarmList) {
            if((alarmData.hour == curTime.hour && alarmData.min >= curTime.minute)
                || alarmData.hour > curTime.hour) {
                tvAlarmTime!!.text = String.format(Locale.getDefault(), "%02d:%02d", alarmData.hour, alarmData.min)
                break
            }
        }
    }

    private fun setTargetRemainView() {
        lifecycleScope.launch {
            val hydrationBeverage = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_BEVERAGE) ?: 0.0f
            val hydrationCoffee = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_COFFEE) ?: 0.0f
            val hydrationWater = (HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_WATER) ?: 0.0f) + (activity as MainActivity).bleService!!.aquaCurValue.toFloat()

            val hydrationValue = hydrationBeverage * 0.8f + hydrationCoffee * 0.9f + hydrationWater
            val targetValue = PreferenceUtil.getProfileTarget(requireActivity())

            val remainValue = max((targetValue - hydrationValue).toInt(), 0)
            tvTargetDone!!.text = String.format(Locale.getDefault(), "%dml", hydrationValue.toInt())
            tvTargetRemain!!.text = String.format(Locale.getDefault(), "%dml", remainValue)
        }
    }
}