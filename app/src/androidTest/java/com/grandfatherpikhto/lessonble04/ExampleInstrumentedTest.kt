package com.grandfatherpikhto.lessonble04

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.lessonble04.ui.MainActivity
import com.grandfatherpikhto.lessonble04.ui.MainActivityTest
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
// import org.mockito.MockitoAnnotations

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val mainActivityRule = ActivityScenarioRule(MainActivity::class.java)
    private val applicationContext = InstrumentationRegistry
        .getInstrumentation().targetContext.applicationContext

    @Before
    fun setUp() {
        // closeable = MockitoAnnotations.openMocks(this)
        mainActivityRule.scenario.onActivity {
        }
    }

    @After
    fun tearDown() {
        // closeable.close()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.grandfatherpikhto.lessonble04", appContext.packageName)
    }
}