package com.easternedgerobotics.rov.value

data class PicameraACpuValue(
        override val frequency: Long = 0,
        override val temperature: Float = 0f,
        override val voltage: Float = 0f
) : CpuValue {
    constructor() : this(0, 0f, 0f)
}
