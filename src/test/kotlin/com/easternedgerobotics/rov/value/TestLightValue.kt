package com.easternedgerobotics.rov.value

data class TestLightValue(override val active: Boolean = false) : LightValue {
    constructor() : this(false)
}
