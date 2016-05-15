package com.easternedgerobotics.rov.value

data class MotionValue(
    val heave: Float = 0f,
    val sway: Float = 0f,
    val surge: Float = 0f,
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f)
