package com.easternedgerobotics.rov.value

data class RasprimeHeartbeatValue(override val operational: Boolean = false) : HeartbeatValue {
    constructor() : this(false)
}
