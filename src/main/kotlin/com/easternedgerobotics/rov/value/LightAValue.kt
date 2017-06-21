package com.easternedgerobotics.rov.value

data class LightAValue(override val active: Boolean = false) : LightValue {
    constructor() : this(false)
}
