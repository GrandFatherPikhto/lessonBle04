package com.grandfatherpikhto.blin.idling

import androidx.test.espresso.IdlingResource
import com.grandfatherpikhto.blin.BleGattManager
import com.grandfatherpikhto.blin.BleManagerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class ConnectingIdling(bleManager: BleManagerInterface) : IdlingResource {
    companion object {
        private var connectingIdling:ConnectingIdling? = null
        fun getInstance(bleManager: BleManagerInterface) : ConnectingIdling {
            return connectingIdling ?: ConnectingIdling(bleManager)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var isIdling = AtomicBoolean(true)

    init {
        scope.launch {
            bleManager.stateFlowConnectState.collect { state ->
                when(state) {
                    BleGattManager.State.Connected -> {
                        isIdling.set(true)
                        resourceCallback?.let { callback ->
                            callback.onTransitionToIdle()
                        }
                    }
                    BleGattManager.State.Connecting -> {
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