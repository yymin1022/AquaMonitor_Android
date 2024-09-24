package com.yong.aquamonitor.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import com.yong.aquamonitor.R
import com.yong.aquamonitor.service.BleService
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

    private var btnReqReset: Button? = null
    private var btnReqUpdate: Button? = null
    private var btnSend: Button? = null
    private var inputValue: EditText? = null
    private var tvValue: TextView? = null

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

        btnReqReset = findViewById(R.id.main_btn_ble_request_reset)
        btnReqUpdate = findViewById(R.id.main_btn_ble_request_update)
        btnSend = findViewById(R.id.main_btn_send)
        inputValue = findViewById(R.id.main_input_value)
        tvValue = findViewById(R.id.main_text_value)
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
        lifecycleScope.launch {
            val hydrationValue = getCurrentHydration(applicationContext)
            tvValue!!.text = String.format(Locale.getDefault(), "Current Hydration Value : %.2f ml", hydrationValue?: -1.0)
        }

        val lastMac = getLastMac()
        if(lastMac != null) {
            tryConnect(lastMac)
        } else {
            startActivity(Intent(applicationContext, ConnectActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isServiceBinded) {
            unbindService(bleServiceConnection)
            isServiceBinded = false
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
                            updateHydration(inputValue!!.text.toString().toInt(), applicationContext)
                        } catch(e: NumberFormatException) {
                            Logger.LogE("Failed to get Hydration Value Input")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        val hydrationValue = getCurrentHydration(applicationContext)
                        tvValue!!.text = String.format(Locale.getDefault(), "Current Hydration Value : %.2f ml", hydrationValue?: -1.0)
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
}