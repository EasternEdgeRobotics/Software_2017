package com.easternedgerobotics.rov.value

data class PrecisionPowerValue(
    val power: Float = 1f,
    val heave: Boolean = true,
    val sway: Boolean = true,
    val surge: Boolean = true,
    val pitch: Boolean = true,
    val yaw: Boolean = true,
    val roll: Boolean = true)
