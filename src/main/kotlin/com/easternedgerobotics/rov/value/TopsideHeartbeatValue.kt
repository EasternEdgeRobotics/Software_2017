package com.easternedgerobotics.rov.value

data class TopsideHeartbeatValue(override val operational: Boolean = false) : HeartbeatValue {
    constructor() : this(false)
}
