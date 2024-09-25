package com.yong.aquamonitor.util

import java.io.Serializable

data class AquaMonitorData(val cycle: Int, val value: Double, val timeFrom: Long, val timeTo: Long, val type: DrinkType?): Serializable

enum class DrinkType {
    DRINK_BEVERAGE,
    DRINK_COFFEE,
    DRINK_ETC,
    DRINK_WATER
}