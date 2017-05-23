package com.easternedgerobotics.rov.value

data class PicameraBHeartbeatValue(override val operational: Boolean = false) : HeartbeatValue {
    constructor() : this(false)
}
