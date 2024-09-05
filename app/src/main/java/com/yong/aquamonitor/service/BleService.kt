package com.yong.aquamonitor.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.yong.aquamonitor.util.Logger

class BleService: Service() {
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}