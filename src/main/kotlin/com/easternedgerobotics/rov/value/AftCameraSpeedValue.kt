package com.easternedgerobotics.rov.value

data class AftCameraSpeedValue(override val speed: Float = 0f) : SpeedValue {
    constructor() : this(0f)
}
