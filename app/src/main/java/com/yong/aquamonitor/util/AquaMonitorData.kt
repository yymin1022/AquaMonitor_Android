package com.yong.aquamonitor.util

import java.io.Serializable

data class AquaMonitorData(val cycle: Int, val value: Double, val timeFrom: Long, val timeTo: Long, var type: DrinkType?, var id: String?): Serializable

enum class DrinkType {
    DRINK_BEVERAGE,
    DRINK_COFFEE,
    DRINK_ETC,
    DRINK_WATER
}