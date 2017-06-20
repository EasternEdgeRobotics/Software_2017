package com.easternedgerobotics.rov.value

data class LightBValue(override val active: Boolean = false) : LightValue {
    constructor() : this(false)
}
