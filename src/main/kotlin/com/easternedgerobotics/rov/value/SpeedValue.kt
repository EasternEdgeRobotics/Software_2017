package com.easternedgerobotics.rov.value

data class SpeedValue(val name: String = "", val speed: Float = 0f) {
    constructor(name: String) : this(name, 0f)

    fun setSpeed(speed: Float) : SpeedValue {
        return this.copy(this.name, speed);
    }
}
