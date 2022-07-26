package com.grandfatherpikhto.lessonble04.ui

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.grandfatherpikhto.lessonble04.LessonBle04App

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import com.grandfatherpikhto.lessonble04.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    companion object {
        const val BLE_NAME="LED_STRIP"
        const val BLE_ADDRESS="01:02:03:04:05:06"
    }

    @get:Rule
    val mainActivityRule = ActivityScenarioRule(MainActivity::class.java)
    private val applicationContext = InstrumentationRegistry
        .getInstrumentation().targetContext.applicationContext


    private val _bleManager by lazy {
        (applicationContext as LessonBle04App).bleManager
    }
    private val bleManager get() = _bleManager!!


    @Before
    fun setUp() {
        mainActivityRule.scenario.onActivity {
            IdlingRegistry.getInstance().register(bleManager.scanner.getScanIdling(name = BLE_NAME))
            IdlingRegistry.getInstance().register(bleManager.connector.getConnectIdling())
            // intending(not(isInternal())).respondWith
        }
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(bleManager.scanner.getScanIdling())
        IdlingRegistry.getInstance().unregister(bleManager.connector.getConnectIdling())
    }

    private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with drawable same as drawable with id $id")
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedBitmap = context.getDrawable(id)?.toBitmap()

            return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
        }
    }

    @Test
    fun connectDevice() {
        onView(withId(R.id.cl_scan)).check(matches(isDisplayed()))
        onView(withId(R.id.action_scan)).perform(click())
        onView(withText(BLE_NAME))
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