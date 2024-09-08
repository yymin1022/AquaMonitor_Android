package com.yong.aquamonitor.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R
import com.yong.aquamonitor.adapter.BleScanRecyclerAdapter
import com.yong.aquamonitor.service.BleService
import com.yong.aquamonitor.util.Logger

class ConnectActivity: AppCompatActivity(), BleScanRecyclerAdapter.OnItemClickListener {
    private var btnStartScan: Button? = null
    private var btnStopScan: Button? = null
    private var progressBar: ProgressBar? = null
    private var recyclerView: RecyclerView? = null

    private var bleAdapter: BluetoothAdapter? = null
    private var bleDevices = mutableListOf<Pair<String, String>>()
    private var bleRecyclerAdapter: BleScanRecyclerAdapter? = null

    private var isBleScanning = false
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
        setContentView(R.layout.activity_connect)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnStartScan = findViewById(R.id.connect_btn_start)
        btnStopScan = findViewById(R.id.connect_btn_stop)
        progressBar = findViewById(R.id.connect_progress_bar)
        recyclerView = findViewById(R.id.connect_recycler_ble)

        btnStartScan!!.setOnClickListener(btnListener)
        btnStopScan!!.setOnClickListener(btnListener)

        progressBar!!.visibility = View.INVISIBLE

        bleAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        bleRecyclerAdapter = BleScanRecyclerAdapter(bleDevices, this)

        recyclerView!!.adapter = bleRecyclerAdapter
        recyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun startScan() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            Logger.LogI("Bluetooth / Location Permission is Not Granted")
            return
        }

        if(!isBleScanning) {
            Logger.LogI("BLE Scan Started")
            isBleScanning = true
            bleAdapter!!.startDiscovery()

            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            registerReceiver(bleScanReceiver, intentFilter)

            bleRecyclerAdapter!!.notifyItemRangeRemoved(0, bleDevices.size)
            bleDevices.clear()

            progressBar!!.visibility = View.VISIBLE
        }
    }

    private fun stopScan() {
        if(isBleScanning) {
            Logger.LogI("BLE Scan Stopped")
            isBleScanning = false

            unregisterReceiver(bleScanReceiver)

            progressBar!!.visibility = View.INVISIBLE
        }
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.connect_btn_start -> startScan()
            R.id.connect_btn_stop -> stopScan()
        }
    }

    private val bleScanReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                BluetoothDevice.ACTION_FOUND -> {
                    if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
                        Logger.LogI("Bluetooth / Location Permission is Not Granted")
                        return
                    }

                    val btDevice = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if(btDevice != null) {
                        Logger.LogD("Bluetooth device discovered. (name = ${btDevice.name},address = ${btDevice.address})")
                        if(btDevice.name != null) {
                            bleDevices.add(Pair(btDevice.name, btDevice.address))
                            bleRecyclerAdapter!!.notifyItemInserted(bleDevices.size)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(device: Pair<String, String>) {
        stopScan()

        val serviceIntent = Intent(applicationContext, BleService::class.java)
        startService(serviceIntent)

        bleDeviceAddress = device.second
        bindService(serviceIntent, bleServiceConnection, Context.BIND_AUTO_CREATE)
    }
}