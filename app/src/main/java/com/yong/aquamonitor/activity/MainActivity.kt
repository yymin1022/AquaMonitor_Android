package com.yong.aquamonitor.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.yong.aquamonitor.R
import com.yong.aquamonitor.service.BleService
import com.yong.aquamonitor.util.AquaMonitorData
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.HealthConnectUtil.getCurrentHydration
import com.yong.aquamonitor.util.HealthConnectUtil.isHealthConnectAvail
import com.yong.aquamonitor.util.HealthConnectUtil.updateHydration
import com.yong.aquamonitor.util.Logger
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val healthPermissionList = setOf(
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class)
    )

    private var btnConnectNew: Button? = null
    private var btnReqReset: Button? = null
    private var btnReqUpdate: Button? = null
    private var btnSend: Button? = null
    private var chartView: PieChart? = null
    private var inputValue: EditText? = null
    private var tvConnectStatus: TextView? = null
    private var tvValue: TextView? = null

    private val bleReceiver = BleReceiver()
    private var bleService: BleService? = null
    private var bleDeviceAddress: String? = null
    private var isServiceBinded = false

    private val bleServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val serviceBinder = service as BleService.LocalBinder
            bleService = serviceBinder.getService()
            isServiceBinded = true
            bleDeviceAddress?.let { id ->
                Logger.LogI("Connecting to [$id]...")
                bleService!!.connectBle(id)
                tvConnectStatus!!.text = String.format("Connecting to [%s]...", id)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isServiceBinded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnConnectNew = findViewById(R.id.main_btn_connect_new)
        btnReqReset = findViewById(R.id.main_btn_ble_request_reset)
        btnReqUpdate = findViewById(R.id.main_btn_ble_request_update)
        btnSend = findViewById(R.id.main_btn_send)
        chartView = findViewById(R.id.main_pie_chart)
        inputValue = findViewById(R.id.main_input_value)
        tvConnectStatus = findViewById(R.id.main_text_connect_status)
        tvValue = findViewById(R.id.main_text_value)
        btnConnectNew!!.setOnClickListener(btnListener)
        btnReqReset!!.setOnClickListener(btnListener)
        btnReqUpdate!!.setOnClickListener(btnListener)
        btnSend!!.setOnClickListener(btnListener)

        if(!isHealthConnectAvail(applicationContext)) {
            Logger.LogE("Health Connect is not Available")
            finish()
        }

        var permissionFlag = false
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(healthPermissionList)) {
                permissionFlag = true
            } else {
                Logger.LogE("Health Connect Permission is not Granted")
            }
        }

        if(!permissionFlag) {
            CoroutineScope(Dispatchers.IO).launch {
                checkPermissionsAndRun(requestPermissions, HealthConnectUtil.getHealthConnectClient(applicationContext))
            }
        }

        Logger.LogI("Successfully Initialized Application")
        readHydrationValue()

        val bleReceiverFilter = IntentFilter().apply {
            addAction(BleService.ACTION_BLE_CONNECTED)
            addAction(BleService.ACTION_BLE_DATA_RECEIVED)
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(bleReceiver, bleReceiverFilter)

        val lastMac = getLastMac()
        if(lastMac != null) {
            tryConnect(lastMac)
        } else {
            startActivity(Intent(applicationContext, ConnectActivity::class.java))
        }

        initChartView()
        setChartView()
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(bleReceiver)
        if(isServiceBinded) {
            unbindService(bleServiceConnection)
            isServiceBinded = false
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

    private fun setChartView() {
        lifecycleScope.launch {
            val hydrationCoffee = 250f
            val hydrationBeverage = 100f
            val hydrationWater = 600f
            val hydrationValue = (hydrationBeverage * 0.8f + hydrationCoffee * 0.9f + hydrationWater)

            val chartValues = arrayListOf(
                PieEntry(hydrationWater, "Water"),
                PieEntry(hydrationCoffee, "Coffee"),
                PieEntry(hydrationBeverage, "Beverage")
            )
            val dataSet = PieDataSet(chartValues, "Test Values")
            dataSet.setDrawValues(false)
            dataSet.colors = listOf(
                Color.valueOf(0.65f, 0.84f, 1f).toArgb(),
                Color.valueOf(0.5f, 0.5f, 0.5f).toArgb(),
                Color.valueOf(0f, 0f, 0f).toArgb()
            )

            chartView!!.maxAngle = if(hydrationValue < 2000) hydrationValue * 360f / 2000 else 360f
            chartView!!.setData(PieData(dataSet))
            chartView!!.animateX(1000, Easing.EaseInOutSine)
        }
    }

    private fun readHydrationValue() {
        lifecycleScope.launch {
            val hydrationValue = getCurrentHydration(applicationContext)
            tvValue!!.text = String.format(Locale.getDefault(), "%.0f%%", if(hydrationValue != null) hydrationValue / 20  else -1)
        }
    }

    private fun getLastMac(): String? {
        return PreferenceUtil.getLastMacAddress(applicationContext)
    }

    private fun tryConnect(mac: String) {
        val serviceIntent = Intent(applicationContext, BleService::class.java)
        startService(serviceIntent)

        bleDeviceAddress = mac
        bindService(serviceIntent, bleServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.main_btn_connect_new -> {
                startActivity(Intent(this, ConnectActivity::class.java))
            }

            R.id.main_btn_ble_request_reset -> {
                bleService?.writeMessage("R")
            }

            R.id.main_btn_ble_request_update -> {
                bleService?.writeMessage("U")
            }

            R.id.main_btn_send -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val tmpData = AquaMonitorData(0, inputValue!!.text.toString().toDouble(), System.currentTimeMillis() - 1, System.currentTimeMillis(), null, null)
                            val savedID = updateHydration(tmpData, applicationContext) ?: return@withContext
                            tmpData.id = savedID

                            PreferenceUtil.saveHealthData(tmpData, applicationContext)
                            Logger.LogD("Saved Data with ID [$savedID]: $tmpData")
                            Logger.LogD("Restored Data from Saved with ID [$savedID]: ${PreferenceUtil.getHealthData(savedID, applicationContext)}")
                        } catch(e: NumberFormatException) {
                            Logger.LogE("Failed to get Hydration Value Input")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        readHydrationValue()
                    }
                }
            }
        }
    }

    private suspend fun checkPermissionsAndRun(requestPermissions: ActivityResultLauncher<Set<String>>, healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if(!granted.containsAll(healthPermissionList)) {
            requestPermissions.launch(healthPermissionList)
        }
    }

    inner class BleReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null) {
                Logger.LogD(intent.action.toString())
                when(intent.action) {
                    BleService.ACTION_BLE_CONNECTED -> {
                        tvConnectStatus!!.text = String.format("Connected to [%s]", intent.getStringExtra("DEVICE_MAC"))
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