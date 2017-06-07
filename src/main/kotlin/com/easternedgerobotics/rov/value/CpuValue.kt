package com.easternedgerobotics.rov.value

interface CpuValue {
    val frequency: Long
    val temperature: Float
    val voltage: Float
}
