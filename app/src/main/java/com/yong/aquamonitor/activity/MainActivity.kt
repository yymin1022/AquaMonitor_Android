package com.yong.aquamonitor.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import com.yong.aquamonitor.R
import com.yong.aquamonitor.fragment.AlarmFragment
import com.yong.aquamonitor.fragment.DetailFragment
import com.yong.aquamonitor.fragment.HomeFragment
import com.yong.aquamonitor.fragment.ProfileFragment
import com.yong.aquamonitor.service.BleService
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.HealthConnectUtil.isHealthConnectAvail
import com.yong.aquamonitor.util.Logger
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val healthPermissionList = setOf(
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class)
    )

    private var bottomNavigation: ConstraintLayout? = null
    private var fragmentLayout: FrameLayout? = null

    var bleService: BleService? = null
    var bleDeviceAddress: String? = null
    var isServiceBinded = false

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

        val lastMac = getLastMac()
        if(lastMac != null) {
            tryConnect(lastMac)
        } else {
            startActivity(Intent(applicationContext, ConnectActivity::class.java))
        }

        bottomNavigation = findViewById(R.id.main_nav)
        fragmentLayout = findViewById(R.id.main_layout_fragment)
        initNavigation()
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

    private fun initNavigation() {
        val navAlarm = findViewById<LinearLayout>(R.id.main_nav_alarm)
        val navDetail = findViewById<LinearLayout>(R.id.main_nav_detail)
        val navHome = findViewById<LinearLayout>(R.id.main_nav_home)
        val navProfile = findViewById<LinearLayout>(R.id.main_nav_profile)

        val navListener = View.OnClickListener { view ->
            when(view.id) {
                R.id.main_nav_alarm -> {
                    supportFragmentManager.beginTransaction().replace(fragmentLayout!!.id, AlarmFragment()).commit()
                }
                R.id.main_nav_detail -> {
                    supportFragmentManager.beginTransaction().replace(fragmentLayout!!.id, DetailFragment()).commit()
                }
                R.id.main_nav_home -> {
                    supportFragmentManager.beginTransaction().replace(fragmentLayout!!.id, HomeFragment()).commit()
                }
                R.id.main_nav_profile -> {
                    supportFragmentManager.beginTransaction().replace(fragmentLayout!!.id, ProfileFragment()).commit()
                }
            }
        }

        supportFragmentManager.beginTransaction().replace(fragmentLayout!!.id, HomeFragment()).commit()
        navAlarm.setOnClickListener(navListener)
        navDetail.setOnClickListener(navListener)
        navHome.setOnClickListener(navListener)
        navProfile.setOnClickListener(navListener)
    }

    private suspend fun checkPermissionsAndRun(requestPermissions: ActivityResultLauncher<Set<String>>, healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if(!granted.containsAll(healthPermissionList)) {
            requestPermissions.launch(healthPermissionList)
        }
    }
}