package com.yong.aquamonitor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BleService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BleService", "Started")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BleService", "Stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}