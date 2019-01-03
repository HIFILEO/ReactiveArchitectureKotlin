/*
Copyright 2017 LEO LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.reactivearchitecture.nowplaying.viewcontroller

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView

import com.example.reactivearchitecture.R

import com.example.reactivearchitecture.nowplaying.adapter.nowplaying.NowPlayingListAdapter
import com.example.reactivearchitecture.core.application.TestReactiveArchitectureApplication
import com.example.reactivearchitecture.nowplaying.view.FilterView
import com.example.reactivearchitecture.nowplaying.service.ServiceApi
import com.example.reactivearchitecture.nowplaying.service.ServiceResponse
import com.example.reactivearchitecture.util.EspressoTestRule
import com.example.reactivearchitecture.util.RecyclerViewItemCountAssertion
import com.example.reactivearchitecture.util.RecyclerViewMatcher
import com.example.reactivearchitecture.util.RxEspressoHandler
import com.example.reactivearchitecture.util.TestEspressoAssetFileHelper
import com.google.gson.Gson

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import java.io.IOException

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins

import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.Is.`is` as IS
import org.assertj.core.api.Assertions.assertThat

@RunWith(AndroidJUnit4::class)
@LargeTest
class NowPlayingActivityTest {

    @Inject
    lateinit var serviceApi: ServiceApi

    @get:Rule
    var activityTestRule = EspressoTestRule(
            NowPlayingActivity::class.java,
            true,
            false // do not start activity
    )

    companion object {
        private lateinit var testReactiveArchitectureApplication:
                TestReactiveArchitectureApplication
        private lateinit var serviceResponse1: ServiceResponse
        private lateinit var serviceResponse2: ServiceResponse
        private val mapToSend1: Map<String, Int> = mapOf("page" to 1)
        private val mapToSend2: Map<String, Int> = mapOf("page" to 2)

        /**
         * Convenience helper to create matcher for recycler view.
         * @param recyclerViewId - ID of [android.support.v7.widget.RecyclerView]
         * @return [RecyclerViewMatcher]
         */
        fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
            return RecyclerViewMatcher(recyclerViewId)
        }

        // Note - without JvmStatic, Junit does not pick anything up.
        @BeforeClass @JvmStatic
        fun setUpClass() {
            //
            // Application Mocking setup, once for all tests in this example
            //

            // Before the activity is launched, get the componentProvider so we can provide our own
            // module for the activity under test.
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            testReactiveArchitectureApplication = instrumentation
                    .targetContext.applicationContext as TestReactiveArchitectureApplication

            // Load JSON data you plan to test with
            // Note - if you wanted to just load it once per test you move this logic.
            var json: String? = null
            var json2: String? = null
            try {
                json = TestEspressoAssetFileHelper.getFileContentAsString(
                        InstrumentationRegistry.getContext(),
                        "now_playing_page_1.json")
                json2 = TestEspressoAssetFileHelper.getFileContentAsString(
                        InstrumentationRegistry.getContext(),
                        "now_playing_page_2.json")
            } catch (e: Exception) {
                fail(e.toString())
            }

            serviceResponse1 = Gson().fromJson(json, ServiceResponse::class.java)
            serviceResponse2 = Gson().fromJson(json2, ServiceResponse::class.java)
        }

        fun <T> withMyValue(name: String): Matcher<T> {

            return object : BaseMatcher<T>() {

                override fun matches(item: Any): Boolean {
                    return item.toString() == name
                }

                override fun describeTo(description: Description) {}
            }
        }
    }

    @Before
    fun setup() {
        // Inject all the application level objects into this test class
        testReactiveArchitectureApplication!!.component!!.inject(this)

        // Every test uses the same JSON response so set it up here once.
        try {
            setupServiceApiMockForData()
        } catch (e: IOException) {
            fail(e.toString())
        }

        // Clear RxHooks
        RxJavaPlugins.reset()
    }

    @Test
    fun appBarShowsTitle() {
        //
        // Arrange
        //

        //
        // Act
        //
        activityTestRule.launchActivity(Intent())

        //
        // Assert
        //
        val name = getResourceString(R.string.now_playing)
        onView(withText(name)).check(matches(isDisplayed()))
    }

    @Test
    fun progressBarShowsWhenFirstStart() {
        //
        // Arrange
        //

        //
        // Act
        //
        activityTestRule.launchActivity(Intent())

        //
        // Assert
        //
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(InterruptedException::class)
    fun adapterHasData() {
        //
        // Arrange
        //

        // Register Rx Idling (Only needed for specific tests that need to wait for data)
        val rxEspressoHandler = RxEspressoHandler()
        RxJavaPlugins.setScheduleHandler(rxEspressoHandler.rxEspressoScheduleHandler)
        RxJavaPlugins.setOnObservableAssembly(rxEspressoHandler.rxEspressoObserverHandler)
        IdlingRegistry.getInstance().register(rxEspressoHandler.idlingResource)

        //
        // Act
        //
        activityTestRule.launchActivity(Intent())

        //
        // Assert
        //
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(20))
    }

    @Test
    @Throws(InterruptedException::class)
    fun progressBarShowsWhenLoadingMoreData() {
        //
        // Arrange
        //

        // Register Rx Idling (Only needed for specific tests that need to wait for data)
        val rxEspressoHandler = RxEspressoHandler()
        RxJavaPlugins.setScheduleHandler(rxEspressoHandler.rxEspressoScheduleHandler)
        RxJavaPlugins.setOnObservableAssembly(rxEspressoHandler.rxEspressoObserverHandler)
        IdlingRegistry.getInstance().register(rxEspressoHandler.idlingResource)

        //
        // Act
        //
        activityTestRule.launchActivity(Intent())

        //
        // Assert
        //
        // Note - because of the idling resource, this check will wait until data is loaded.
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(20))

        // unregister so we can do checks without waiting for data
        IdlingRegistry.getInstance().unregister(rxEspressoHandler.idlingResource)

        // Scroll to the bottom to trigger the progress par.
        onView(withId(R.id.recyclerView)).perform(
            // scroll to bottom so progress spinner gets added
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(19),
            // scroll to show progress spinner
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(20)
        )

        // Not ideal - need to wait until next frame to trigger load
        // TODO - create an espresso test and wait w/ backoff.
        Thread.sleep(250)

        // Note - without idling resource, we can check for progress item while data loads.
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(21))

        onView(withRecyclerView(R.id.recyclerView).atPosition(20))
                .check(matches(hasDescendant(withId(R.id.progressBar))))
    }

    @Test
    @Throws(InterruptedException::class)
    fun filterOnLoadsMoreData() {
        //
        // Arrange
        //

        // Register Rx Idling (Only needed for specific tests that need to wait for data)
        val rxEspressoHandler = RxEspressoHandler()
        RxJavaPlugins.setScheduleHandler(rxEspressoHandler.rxEspressoScheduleHandler)
        RxJavaPlugins.setOnObservableAssembly(rxEspressoHandler.rxEspressoObserverHandler)

        //
        // Act
        //
        activityTestRule.launchActivity(Intent())
        onView(withId(R.id.filterSpinner)).perform(click())

        onData(allOf(IS(instanceOf<Any>(FilterView::class.java)),
                object : BaseMatcher<FilterView>() {
            override fun describeTo(description: Description) {
                // No-Op
            }

            override fun matches(item: Any): Boolean {
                return if (item is FilterView) {
                    if (item.filterText.equals("On", ignoreCase = true)) {
                        true
                    } else {
                        false
                    }
                } else false
            }
        })).perform(click())

        // enable idling resource AFTER we trigger the filter.
        IdlingRegistry.getInstance().register(rxEspressoHandler.idlingResource)

        //
        // Assert
        //

        // A load more will trigger when not enough data to fill screen.
        // Check the second page loads a total of 7 items.
        onView(withId(R.id.recyclerView)).check { view, noViewFoundException ->
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertThat(adapter!!.itemCount, Matchers.`is`(10))

            assertThat(adapter).isInstanceOf(NowPlayingListAdapter::class.java)
            val nowPlayingListAdapter = adapter as NowPlayingListAdapter?
            assertThat(nowPlayingListAdapter!!.getItem(6)).isNotNull()
        }
    }

    @After
    fun tearDown() {
        // Note - you must remove the idling resources after each Test
        val idlingResourceCollection = IdlingRegistry.getInstance().resources
        for (idlingResource in idlingResourceCollection) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }

    private fun getResourceString(id: Int): String {
        val targetContext = InstrumentationRegistry.getTargetContext()
        return targetContext.resources.getString(id)
    }

    /**
     * Setup the serviceApi mock to mock backend calls.
     * @throws IOException -
     */
    @Throws(IOException::class)
    private fun setupServiceApiMockForData() {
        // Since the 'serviceApi' is an application singleton, we must reset it for every test.
        Mockito.reset<ServiceApi>(serviceApi)

        val apiKey = getResourceString(R.string.api_key)
        whenever(serviceApi.nowPlaying(apiKey, mapToSend1))
                .thenReturn(Observable.just(serviceResponse1))
        whenever(serviceApi.nowPlaying(apiKey, mapToSend2))
                .thenReturn(Observable.just(serviceResponse2))
    }
}
