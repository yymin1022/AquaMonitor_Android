package com.yong.aquamonitor.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yong.aquamonitor.R
import com.yong.aquamonitor.activity.ConnectActivity
import com.yong.aquamonitor.activity.MainActivity
import com.yong.aquamonitor.service.BleService
import com.yong.aquamonitor.util.DrinkType
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment: Fragment() {
    private var btnConnectNew: ImageButton? = null
    private var btnReqReset: ImageButton? = null
    private var btnReqUpdate: ImageButton? = null
    private var chartView: PieChart? = null
    private var tvConnectStatus: TextView? = null
    private var tvHydrationBeveragePerc: TextView? = null
    private var tvHydrationBeverageValue: TextView? = null
    private var tvHydrationCoffeePerc: TextView? = null
    private var tvHydrationCoffeeValue: TextView? = null
    private var tvHydrationWaterPerc: TextView? = null
    private var tvHydrationWaterValue: TextView? = null
    private var tvValue: TextView? = null

    private val bleReceiver = BleReceiver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutInflater = inflater.inflate(R.layout.fragment_home, container, false)
        btnConnectNew = layoutInflater.findViewById(R.id.home_btn_connect_new)
        btnReqReset = layoutInflater.findViewById(R.id.home_btn_ble_request_reset)
        btnReqUpdate = layoutInflater.findViewById(R.id.home_btn_ble_request_update)
        chartView = layoutInflater.findViewById(R.id.home_pie_chart)
        tvConnectStatus = layoutInflater.findViewById(R.id.home_text_connect_status)
        tvHydrationBeveragePerc = layoutInflater.findViewById(R.id.home_card_perc_beverage)
        tvHydrationBeverageValue = layoutInflater.findViewById(R.id.home_card_value_beverage)
        tvHydrationCoffeePerc = layoutInflater.findViewById(R.id.home_card_perc_coffee)
        tvHydrationCoffeeValue = layoutInflater.findViewById(R.id.home_card_value_coffee)
        tvHydrationWaterPerc = layoutInflater.findViewById(R.id.home_card_perc_water)
        tvHydrationWaterValue = layoutInflater.findViewById(R.id.home_card_value_water)
        tvValue = layoutInflater.findViewById(R.id.home_text_value)

        btnConnectNew!!.setOnClickListener(btnListener)
        btnReqReset!!.setOnClickListener(btnListener)
        btnReqUpdate!!.setOnClickListener(btnListener)

        initChartView()
        readHydrationValue()

        val bleReceiverFilter = IntentFilter().apply {
            addAction(BleService.ACTION_BLE_CONNECTED)
            addAction(BleService.ACTION_BLE_DATA_RECEIVED)
        }
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(bleReceiver, bleReceiverFilter)

        return layoutInflater
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(bleReceiver)
    }

    private fun readHydrationValue() {
        lifecycleScope.launch {
            val hydrationBeverage = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_BEVERAGE) ?: 0.0f
            val hydrationCoffee = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_COFFEE) ?: 0.0f
            val hydrationWater = HealthConnectUtil.getTodayHydrationByType(requireActivity(), DrinkType.DRINK_WATER) ?: 0.0f

            val hydrationValue = hydrationBeverage * 0.8f + hydrationCoffee * 0.9f + hydrationWater
            setChartView(hydrationValue, hydrationBeverage * 0.8f, hydrationCoffee * 0.9f, hydrationWater)

            tvHydrationBeveragePerc!!.text = String.format(Locale.getDefault(), "%d%%", hydrationBeverage.toInt() / 20)
            tvHydrationBeverageValue!!.text = String.format(Locale.getDefault(), "%dml", hydrationBeverage.toInt())
            tvHydrationCoffeePerc!!.text = String.format(Locale.getDefault(), "%d%%", hydrationCoffee.toInt() / 20)
            tvHydrationCoffeeValue!!.text = String.format(Locale.getDefault(), "%dml", hydrationCoffee.toInt())
            tvHydrationWaterPerc!!.text = String.format(Locale.getDefault(), "%d%%", hydrationWater.toInt() / 20)
            tvHydrationWaterValue!!.text = String.format(Locale.getDefault(), "%dml", hydrationWater.toInt())
            tvValue!!.text = String.format(Locale.getDefault(), "%.0f%%",  hydrationValue / 20)
        }
    }

    private fun initChartView() {
        chartView!!.description.isEnabled = false
        chartView!!.holeRadius = 85f
        chartView!!.isRotationEnabled = false
        chartView!!.legend.isEnabled = false
        chartView!!.setBackgroundColor(Color.TRANSPARENT)
        chartView!!.setDrawEntryLabels(false)
        chartView!!.setHoleColor(Color.TRANSPARENT)
        chartView!!.setTouchEnabled(false)
    }

    private fun setChartView(hydrationValue: Float, hydrationBeverage: Float, hydrationCoffee: Float, hydrationWater: Float) {
        lifecycleScope.launch {
            val chartValues = arrayListOf(
                PieEntry(hydrationWater, "Water"),
                PieEntry(hydrationCoffee, "Coffee"),
                PieEntry(hydrationBeverage, "Beverage")
            )
            val dataSet = PieDataSet(chartValues, "Test Values")
            dataSet.setDrawValues(false)
            dataSet.colors = listOf(
                requireActivity().getColor(R.color.hydration_water),
                requireActivity().getColor(R.color.hydration_coffee),
                requireActivity().getColor(R.color.hydration_beverage)
            )

            chartView!!.maxAngle = if(hydrationValue < 2000) hydrationValue * 360f / 2000 else 360f
            chartView!!.setData(PieData(dataSet))
            chartView!!.animateX(1000, Easing.EaseInOutSine)
        }
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.home_btn_connect_new -> {
                startActivity(Intent(activity, ConnectActivity::class.java))
            }

            R.id.home_btn_ble_request_reset -> {
                (activity as MainActivity).bleService?.writeMessage("R")
                readHydrationValue()
            }

            R.id.home_btn_ble_request_update -> {
                (activity as MainActivity).bleService?.writeMessage("U")
                readHydrationValue()
            }
        }
    }

    inner class BleReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null) {
                Logger.LogD(intent.action.toString())
                when(intent.action) {
                    BleService.ACTION_BLE_CONNECTED -> {
                        tvConnectStatus!!.text = String.format("Connected to [%s]", intent.getStringExtra("DEVICE_NAME"))
                    }

                    BleService.ACTION_BLE_DATA_RECEIVED -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            readHydrationValue()
                        }
                    }
                }
            }
        }
    }
}