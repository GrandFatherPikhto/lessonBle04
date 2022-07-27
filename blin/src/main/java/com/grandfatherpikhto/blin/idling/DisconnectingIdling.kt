package com.grandfatherpikhto.blin.idling

import androidx.test.espresso.IdlingResource
import com.grandfatherpikhto.blin.BleGattManager
import com.grandfatherpikhto.blin.BleManagerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class DisconnectingIdling (private val bleManager: BleManagerInterface): IdlingResource {
    companion object {
        private var disconnectingIdling:DisconnectingIdling? = null
        fun getInstance(bleManager: BleManagerInterface) : DisconnectingIdling {
            return disconnectingIdling ?: DisconnectingIdling(bleManager)
        }
    }

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var isIdling = AtomicBoolean(true)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            bleManager.stateFlowConnectState.collect { state ->
                when(state) {
                    BleGattManager.State.Disconnected -> {
                        isIdling.set(true)
                        resourceCallback?.let { callback ->
                            callback.onTransitionToIdle()
                        }
                    }
                    BleGattManager.State.Disconnecting -> {
                        isIdling.set(false)
                    }
                    else -> { }
                }
            }
        }
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun isIdleNow(): Boolean = isIdling.get()

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}