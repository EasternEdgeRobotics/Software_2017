package com.easternedgerobotics.rov.value

data class ExternalPressureValue(override val pressure: Float = 0f) : PressureValue {
    constructor() : this(0f)
}
