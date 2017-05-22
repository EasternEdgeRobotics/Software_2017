package com.easternedgerobotics.rov.value

data class ArduinoHeartbeatValue(override val operational: Boolean = false) : HeartbeatValue {
    constructor() : this(false)
}
