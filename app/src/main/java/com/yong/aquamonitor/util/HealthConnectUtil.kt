package com.yong.aquamonitor.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.deleteRecords
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
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

    suspend fun deleteHydration(id: String, context: Context) {
        initHealthConnect(context)

        Logger.LogI("Deleting Health Data [$id]")

        try {
            healthConnectClient!!.deleteRecords<HydrationRecord>(
                recordIdsList = listOf(id),
                clientRecordIdsList = emptyList()
            )
        } catch(e: Exception) {
            Logger.LogE("Health Connect Set Error: [$e]")
        }
    }

    suspend fun getTodayHydration(context: Context): Double? {
        initHealthConnect(context)

        var hydrationValue: Double? = null
        Logger.LogI("Reading Current Value...")
        withContext(Dispatchers.IO) {
            try {
                hydrationValue = 0.0
                val recordRequest: ReadRecordsRequest<HydrationRecord> = ReadRecordsRequest(TimeRangeFilter.after(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
                async {
                    healthConnectClient!!.readRecords(recordRequest)
                }.await().records.forEach { record ->
                    hydrationValue = hydrationValue!! + record.volume.inMilliliters
                    Logger.LogI(record.metadata.id)
                }
            } catch(e: Exception) {
                Logger.LogE("Health Connect Get Error: [$e]")
            }
        }

        return hydrationValue
    }

    suspend fun getTodayHydrationByType(context: Context, type: DrinkType): Float? {
        initHealthConnect(context)

        var hydrationValue: Float? = null
        Logger.LogI("Reading Current Value...")
        withContext(Dispatchers.IO) {
            try {
                hydrationValue = 0.0f
                val recordRequest: ReadRecordsRequest<HydrationRecord> = ReadRecordsRequest(TimeRangeFilter.after(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
                async {
                    healthConnectClient!!.readRecords(recordRequest)
                }.await().records.forEach { record ->
                    val curRecordData = PreferenceUtil.getHealthData(record.metadata.id, context)
                    if(curRecordData?.type != null && curRecordData.type == type) {
                        hydrationValue = hydrationValue!! + record.volume.inMilliliters.toFloat()
                    }
                    Logger.LogI(record.metadata.id)
                }
            } catch(e: Exception) {
                Logger.LogE("Health Connect Get Error: [$e]")
            }
        }

        return hydrationValue
    }

    suspend fun getTotalHydrationData(context: Context): MutableList<AquaMonitorData> {
        initHealthConnect(context)

        val dataList = mutableListOf<AquaMonitorData>()
        Logger.LogI("Reading Current Value...")
        withContext(Dispatchers.IO) {
            try {
                val recordRequest: ReadRecordsRequest<HydrationRecord> = ReadRecordsRequest(TimeRangeFilter.before(LocalDateTime.now()))
                async {
                    healthConnectClient!!.readRecords(recordRequest)
                }.await().records.forEach { record ->
                    val curRecordData = PreferenceUtil.getHealthData(record.metadata.id, context)
                    if(curRecordData != null) {
                        dataList.add(curRecordData)
                    }
                    Logger.LogI(record.metadata.id)
                }
            } catch(e: Exception) {
                Logger.LogE("Health Connect Get Error: [$e]")
            }
        }

        return dataList
    }

    suspend fun updateHydration(data: AquaMonitorData, context: Context): String? {
        initHealthConnect(context)

        Logger.LogI("Updating Current Value with ${data.value}...")

        try {
            val hydrationRecord = HydrationRecord(
                volume = Volume.milliliters(data.value),
                startTime = Instant.ofEpochMilli(data.timeFrom),
                endTime = Instant.ofEpochMilli(data.timeTo),
                startZoneOffset = null,
                endZoneOffset = null
            )
            val insertResult = healthConnectClient!!.insertRecords(listOf(hydrationRecord))
            for(res in insertResult.recordIdsList) {
                return res
            }
        } catch(e: Exception) {
            Logger.LogE("Health Connect Set Error: [$e]")
        }

        return null
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