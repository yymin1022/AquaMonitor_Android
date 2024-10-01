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
import java.util.Locale
import kotlin.math.max

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

        setNextAlarmView()
        setTargetRemainView()

        return layoutInflater
    }

    private fun setNextAlarmView() {

    }

    private fun setTargetRemainView() {
        lifecycleScope.launch {
            val hydrationBeverage = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_BEVERAGE) ?: 0.0f
            val hydrationCoffee = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_COFFEE) ?: 0.0f
            val hydrationWater = (HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_WATER) ?: 0.0f) + (activity as MainActivity).bleService!!.aquaCurValue.toFloat()

            val hydrationValue = hydrationBeverage * 0.8f + hydrationCoffee * 0.9f + hydrationWater
            val targetValue = PreferenceUtil.getProfileTarget(requireActivity())

            val remainValue = max((targetValue - hydrationValue).toInt(), 0)
            tvTargetRemain!!.text = String.format(Locale.getDefault(), "오늘은 %dml 더 마사면 목표를 달성해요!", remainValue)
        }
    }
}