package com.yong.aquamonitor.util

import android.content.Context
import android.content.SharedPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Base64


object PreferenceUtil {
    private var pref: SharedPreferences? = null

    private fun initPreference(context: Context) {
        if(pref == null) {
            pref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        }
    }

    fun getHealthData(id: String, context: Context): AquaMonitorData? {
        initPreference(context)
        val dataEncoded = pref!!.getString(id, null) ?: return null
        try {
            ByteArrayInputStream(Base64.getDecoder().decode(dataEncoded)).use { byteArrayInputStream ->
                ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                    return objectInputStream.readObject() as AquaMonitorData
                }
            }
        } catch (e: java.lang.Exception) {
            Logger.LogE("Error when saving: $e")
            return null
        }
    }

    fun saveHealthData(data: AquaMonitorData, context: Context) {
        initPreference(context)

        val dataID = data.id ?: return
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(data)
                    val dataEncoded = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())

                    val prefEd = pref!!.edit()
                    prefEd.putString(dataID, dataEncoded)
                    prefEd.apply()
                }
            }
        } catch (e: Exception) {
            Logger.LogE("Error when saving: $e")
            return
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