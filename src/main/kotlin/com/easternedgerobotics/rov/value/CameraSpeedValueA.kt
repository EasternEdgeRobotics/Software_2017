package com.easternedgerobotics.rov.value

data class CameraSpeedValueA(override val speed: Float = 0f) : SpeedValue {
    constructor() : this(0f)
}
