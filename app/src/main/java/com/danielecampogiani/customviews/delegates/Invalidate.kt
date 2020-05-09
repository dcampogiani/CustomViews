package com.danielecampogiani.customviews.delegates

import android.view.View
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Invalidate<T>
private constructor(initialValue: T) : ReadWriteProperty<View, T> {

    companion object {
        fun <T> T.invalidateOnChange() =
            Invalidate(this)
    }

    private var value: T = initialValue

    override fun getValue(thisRef: View, property: KProperty<*>): T = value

    override fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.invalidate()
    }
}