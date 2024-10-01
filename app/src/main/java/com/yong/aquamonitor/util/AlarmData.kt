package com.yong.aquamonitor.util

import java.io.Serializable

data class AlarmData(val hour: Int, val min: Int, val value: Int): Serializable
