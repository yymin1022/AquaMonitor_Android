package com.yong.aquamonitor.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset

object HealthConnectUtil {
    private var healthConnectClient: HealthConnectClient? = null

    private fun initHealthConnect(context: Context) {
        if(healthConnectClient == null) {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
        }
    }

    fun getHealthConnectClient(context: Context): HealthConnectClient {
        initHealthConnect(context)
        return healthConnectClient!!
    }

    suspend fun getCurrentHydration(context: Context): Double? {
        initHealthConnect(context)

        var hydrationValue: Double? = null

        withContext(Dispatchers.IO) {
            Logger.LogI("Reading Current Value...")
            withContext(Dispatchers.IO) {
                try {
                    val hydrationResponse = async { healthConnectClient!!.aggregate(
                        AggregateRequest(
                            metrics = setOf(HydrationRecord.VOLUME_TOTAL),
                            timeRangeFilter = TimeRangeFilter.before(Instant.now())
                        )
                    ) }.await()[HydrationRecord.VOLUME_TOTAL]
                    hydrationValue = hydrationResponse?.inMilliliters
                } catch(e: Exception) {
                    Logger.LogE("Health Connect Get Error: [$e]")
                }
            }
        }

        return hydrationValue
    }

    suspend fun updateHydration(data: AquaMonitorData, context: Context) {
        initHealthConnect(context)

        Logger.LogI("Updating Current Value with ${data.value}...")

        try {
            val hydrationRecord = HydrationRecord(
                volume = Volume.milliliters(data.value),
                startTime = Instant.ofEpochMilli(data.timeFrom),
                endTime = Instant.ofEpochMilli(data.timeTo),
                startZoneOffset = ZoneOffset.MIN,
                endZoneOffset = ZoneOffset.MIN,
                metadata = Metadata(
                    id = Instant.ofEpochMilli(data.timeFrom).toString(),
                    DataOrigin("com.yong.aquamonitor"),
                    Instant.ofEpochMilli(data.timeTo))
            )
            val insertResult = healthConnectClient!!.insertRecords(listOf(hydrationRecord))
            for(res in insertResult.recordIdsList) {
                Logger.LogI("Inserted $res")
            }
        } catch(e: Exception) {
            Logger.LogE("Health Connect Set Error: [$e]")
        }
    }

    fun isHealthConnectAvail(context: Context): Boolean {
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
}