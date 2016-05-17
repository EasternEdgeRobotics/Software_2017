package com.easternedgerobotics.rov.value

data class StarboardVertSpeedValue(override val speed: Float = 0f) : SpeedValue {
    constructor() : this(0f)
}
