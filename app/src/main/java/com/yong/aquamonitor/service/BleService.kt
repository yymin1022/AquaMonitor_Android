package com.yong.aquamonitor.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.yong.aquamonitor.util.AquaMonitorData
import com.yong.aquamonitor.util.HealthConnectUtil
import com.yong.aquamonitor.util.Logger
import com.yong.aquamonitor.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class BleService: Service() {
    companion object {
        const val ACTION_BLE_CONNECTED = "AQUAMONITOR_CONNECTED"
        const val ACTION_BLE_DATA_RECEIVED = "AQUAMONITOR_DATA_RECEIVED"

        const val UUID_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb"
        const val UUID_DESCRIPTOR = "00002901-0000-1000-8000-00805f9b34fb"
        const val UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
    }

    private var aquaCurCycle = -1
    private var aquaCurStartTime = 0L
    var aquaCurValue = 0.0

    private var bleAdapter: BluetoothAdapter? = null
    var bleGatt: BluetoothGatt? = null

    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        fun getService(): BleService = this@BleService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.LogD("Started")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.LogD("Stopped")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun connectBle(deviceAddr: String): Boolean {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            Logger.LogI("Bluetooth / Location Permission is Not Granted")
            return false
        }

        if(bleAdapter == null && bleGatt == null) {
            bleAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            val bleDevice = bleAdapter!!.getRemoteDevice(deviceAddr)
            bleGatt = bleDevice.connectGatt(this, true, gattCallback, 2)
        }

        return true
    }

    @Suppress("DEPRECATION")
    fun writeMessage(data: String) {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            Logger.LogI("Bluetooth / Location Permission is Not Granted")
            return
        }

        try {
            bleGatt?.let { gatt ->
                val service = gatt.getService(UUID.fromString(UUID_SERVICE))
                if(service == null) {
                    Logger.LogE("Service Not Found")
                }

                val characteristic = service?.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC))
                if(characteristic != null) {
                    val value = data.toByteArray(Charsets.UTF_8)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    } else {
                        characteristic.value = value
                        gatt.writeCharacteristic(characteristic)
                    }
                    Logger.LogI("Message [${value.contentToString()}] sent")
                } else {
                    Logger.LogE("Characteristic Not Found")
                }
            }
        } catch(e: Exception) {
            Logger.LogE("Write Error: $e")
        }
    }

    private fun processData(cycle: Int, value: Double) {
        if(aquaCurCycle < 0) {
            aquaCurCycle = cycle
            aquaCurStartTime = System.currentTimeMillis()
            aquaCurValue = value
        } else if(aquaCurCycle == cycle) {
            aquaCurValue = value
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                saveData(cycle, value)
            }
        }
    }

    private suspend fun saveData(cycle: Int, value: Double) {
        val curTime = System.currentTimeMillis()
        val tmpData = AquaMonitorData(aquaCurCycle, aquaCurValue, aquaCurStartTime, curTime, null, null)

        val savedID = HealthConnectUtil.updateHydration(tmpData, applicationContext) ?: return
        tmpData.id = savedID

        PreferenceUtil.saveHealthData(tmpData, applicationContext)
        Logger.LogD("Saved Data with ID [$savedID]: $tmpData")
        Logger.LogD("Restored Data from Saved with ID [$savedID]: ${PreferenceUtil.getHealthData(savedID, applicationContext)}")
        
        aquaCurCycle = cycle
        aquaCurStartTime = curTime
        aquaCurValue = value
    }

    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                Logger.LogI("Bluetooth / Location Permission is Not Granted")
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.LogI("Device Connected")
                gatt.discoverServices()

                val intent = Intent(ACTION_BLE_CONNECTED).apply {
                    putExtra("DEVICE_MAC", gatt.device!!.address)
                    putExtra("DEVICE_NAME", gatt.device!!.name)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Logger.LogI("Device Disconnected")
                bleGatt = null
            }
        }

        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                Logger.LogI("Bluetooth / Location Permission is Not Granted")
                return
            }

            Logger.LogI("Discovering Service")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Logger.LogI("Discovered Service")
                val service = gatt.getService(UUID.fromString(UUID_SERVICE))
                val characteristic = service?.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC))
                if(service != null && characteristic != null) {
                    Logger.LogI("Finding Response Characteristic")
                    gatt.setCharacteristicNotification(characteristic, true)

                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    val descriptor = characteristic.getDescriptor(UUID.fromString(UUID_DESCRIPTOR))
                    if(descriptor != null) {
                        Logger.LogI("Enabled Notify Receive")

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION") val data = characteristic.value?.toString(Charsets.UTF_8)
            if(data != null) {
                val dataList = data.split("\n").map { value -> value.trim() }

                val receivedCycle = dataList[1].toInt()
                val receivedValue = dataList[0].toDouble()
                Logger.LogI("Message Received: [${receivedCycle} / ${receivedValue}]")

                processData(receivedCycle, receivedValue)
                sendBroadcast(Intent(ACTION_BLE_DATA_RECEIVED))
            }
        }
    }
}