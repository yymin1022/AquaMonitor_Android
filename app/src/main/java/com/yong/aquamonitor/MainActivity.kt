package com.yong.aquamonitor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "AquaMonitor"
    private val PERMISSION_LIST = setOf(
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class)
    )

    private var healthConnectClient: HealthConnectClient? = null

    private var btnSend: Button? = null
    private var inputValue: EditText? = null
    private var tvValue: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSend = findViewById(R.id.main_btn_send)
        inputValue = findViewById(R.id.main_input_value)
        tvValue = findViewById(R.id.main_text_value)
        btnSend!!.setOnClickListener(btnListener)

        if(!isHealthConnectAvail(applicationContext)) {
            Log.e(LOG_TAG, "Health Connect is not Available")
            finish()
        }

        healthConnectClient = getHealthConnectClient(applicationContext)

        var permissionFlag = false
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(PERMISSION_LIST)) {
                permissionFlag = true
            } else {
                Log.e(LOG_TAG, "Health Connect Permission is not Granted")
            }
        }

        if(!permissionFlag) {
            CoroutineScope(Dispatchers.IO).launch {
                checkPermissionsAndRun(requestPermissions, healthConnectClient!!)
            }
        }

        Log.i(LOG_TAG, "Successfully Initialized Application")
    }

    private suspend fun getCurrentHydration() {
        Log.i(LOG_TAG, "Reading Current Value...")
    }

    private suspend fun updateHydration(value: Int) {
        Log.i(LOG_TAG, "Updating Current Value with $value...")
    }

    private fun isHealthConnectAvail(context: Context): Boolean {
        val availStatus = HealthConnectClient.getSdkStatus(context)
        if(availStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return false
        }
        if(availStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            val vendingUrl = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(vendingUrl)
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }
            )
            return false
        }

        return true
    }

    private fun getHealthConnectClient(context: Context): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }

    private suspend fun checkPermissionsAndRun(requestPermissions: ActivityResultLauncher<Set<String>>, healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if(!granted.containsAll(PERMISSION_LIST)) {
            requestPermissions.launch(PERMISSION_LIST)
        }
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.main_btn_send -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        updateHydration(inputValue!!.text.toString().toInt())
                    } catch(e: NumberFormatException) {
                        Log.e(LOG_TAG, "Failed to get Hydration Value Input")
                    }

                }
            }
        }
    }
}