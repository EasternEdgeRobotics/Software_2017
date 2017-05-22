package com.easternedgerobotics.rov.value

data class PicameraAHeartbeatValue(override val operational: Boolean = false) : HeartbeatValue {
    constructor() : this(false)
}
