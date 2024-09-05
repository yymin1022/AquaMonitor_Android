package com.yong.aquamonitor.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {
    private var pref: SharedPreferences? = null

    private fun initPreference(context: Context) {
        if(pref == null) {
            pref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        }
    }

    fun getLastMacAddress(context: Context): String? {
        initPreference(context)
        return pref!!.getString("MAC_ADDRESS", null)
    }

    fun setMacAddress(context: Context, mac: String) {
        initPreference(context)

        val prefEd = pref!!.edit()
        prefEd.putString("MAC_ADDRESS", mac)
        prefEd.apply()
    }
}