package com.yong.aquamonitor.util

import android.util.Log

object Logger {
    private const val LOG_TAG = "AquaMonitor"

    fun LogD(msg: String) {
        Log.d(LOG_TAG, msg)
    }

    fun LogE(msg: String) {
        Log.e(LOG_TAG, msg)
    }

    fun LogI(msg: String) {
        Log.i(LOG_TAG, msg)
    }
}