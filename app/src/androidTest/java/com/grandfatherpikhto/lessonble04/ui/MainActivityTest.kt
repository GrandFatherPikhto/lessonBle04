package com.grandfatherpikhto.lessonble04.ui

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.FakeBleManager
import com.grandfatherpikhto.lessonble04.LessonBle04App

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.helper.withDrawable
import org.junit.Test


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private val argIntent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .putExtra(MainActivity.FAKE, true)

    @get:Rule
    val mainActivityRule = activityScenarioRule<MainActivity>(argIntent)
    private val applicationContext = InstrumentationRegistry
        .getInstrumentation().targetContext.applicationContext


    private val _bleManager by lazy {
        (applicationContext as LessonBle04App).bleManager
    }
    private val bleManager get() = _bleManager!!


    @Before
    fun setUp() {
        mainActivityRule.scenario.onActivity { }
        IdlingRegistry.getInstance().register((bleManager as FakeBleManager)
            .scanIdling)
        IdlingRegistry.getInstance().register((bleManager as FakeBleManager)
            .connectingIdling)
        IdlingRegistry.getInstance().register((bleManager as FakeBleManager)
            .disconnectingIdling)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister((bleManager as FakeBleManager)
            .scanIdling)
        IdlingRegistry.getInstance().unregister((bleManager as FakeBleManager)
            .connectingIdling)
        IdlingRegistry.getInstance().unregister((bleManager as FakeBleManager)
            .disconnectingIdling)
    }

    @Test(timeout = 15000)
    fun connectDevice() {
        onView(withId(R.id.cl_scan_fragment)).check(matches(isDisplayed()))
        onView(withId(R.id.action_scan)).perform(click())
        val scanResult = bleManager.scanResults.filter { it.isConnectable }[0]
        onView(withText(scanResult.device.name))
            .check(matches(isDisplayed()))
            .perform(click())
        onView(withId(R.id.cl_device)).check(matches(isDisplayed()))
        onView(withId(R.id.iv_ble_connected))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_connect_big)))
        onView(withId(R.id.action_connect)).perform(click())
        onView(withId(R.id.iv_ble_connected))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_disconnect_big)))
    }
}