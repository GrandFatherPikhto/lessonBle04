package com.grandfatherpikhto.blin.idling

import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class BleIdling : IdlingResource {
    companion object {
        private var bleIdling:BleIdling? = null
        fun getInstance() : BleIdling {
            return bleIdling ?: BleIdling()
        }
    }

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var isNavigated = AtomicBoolean(true)

    var completed by Delegates.observable(true) { _, _, newState ->
        isNavigated.set(newState)
        resourceCallback?.let { callback ->
            if (newState) {
                callback.onTransitionToIdle()
            }
        }
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun isIdleNow(): Boolean = isNavigated.get()

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}