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
package com.example.reactivearchitecture.util

import android.app.Activity
import android.app.Instrumentation
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import android.util.Log

import com.google.common.base.Throwables
import com.google.common.collect.Sets

import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference

import com.google.common.base.Throwables.throwIfUnchecked

/**
 * Custom test runner to make sure previous activities are completed (meaning they go through
 * the entire lifecycle) before starting next test.
 *
 * There is a bug in espresso 2.2.2 where previous activity under test does NOT become destroyed
 * until the next activity test is started. This is used for for 3.0.0.
 * https://issuetracker.google.com/issues/37082857
 *
 * Inspired from:
 * https://gist.github.com/patrickhammond/19e584b90d7aae20f8f4
 *
 * @param <T>
</T> */
class EspressoTestRule<T : Activity> : ActivityTestRule<T> {

    constructor(activityClass: Class<T>) : super(activityClass) {}

    constructor(activityClass: Class<T>, initialTouchMode: Boolean) :
        super(activityClass, initialTouchMode) {}

    constructor(activityClass: Class<T>, initialTouchMode: Boolean, launchActivity: Boolean) :
        super(activityClass, initialTouchMode, launchActivity) {}

    override fun apply(base: Statement, description: Description?): Statement {

        try {
            return super.apply(base, description)
        } finally {
            closeAllActivities()
        }
    }

    // See https://code.google.com/p/android-test-kit/issues/detail?id=66
    private fun closeAllActivities() {
        try {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            closeAllActivities(instrumentation)
        } catch (ex: Exception) {
            Log.e(TAG, "Could not use close all activities", ex)
        }
    }

    @Throws(Exception::class)
    private fun closeAllActivities(instrumentation: Instrumentation) {
        val NUMBER_OF_RETRIES = 100
        var i = 0
        while (closeActivity(instrumentation)) {
            if (i++ > NUMBER_OF_RETRIES) {
                throw AssertionError("Limit of retries excesses")
            }
            Thread.sleep(200)
        }
    }

    @Throws(Exception::class)
    private fun closeActivity(instrumentation: Instrumentation): Boolean {
        val activityClosed = callOnMainSync(instrumentation, Callable {
            val activities = getActivitiesInStages(Stage.RESUMED,
                    Stage.STARTED, Stage.PAUSED, Stage.STOPPED, Stage.CREATED)
            activities.removeAll(getActivitiesInStages(Stage.DESTROYED))
            if (activities.size > 0) {
                val activity = activities.iterator().next()
                activity.finish()
                true
            } else {
                false
            }
        })
        if (activityClosed!!) {
            instrumentation.waitForIdleSync()
        }
        return activityClosed
    }

    @Throws(Exception::class)
    private fun <X> callOnMainSync(instrumentation: Instrumentation, callable: Callable<X>): X? {
        val retAtomic = AtomicReference<X>()
        val exceptionAtomic = AtomicReference<Throwable>()
        instrumentation.runOnMainSync {
            try {
                retAtomic.set(callable.call())
            } catch (e: Throwable) {
                exceptionAtomic.set(e)
            }
        }
        val exception = exceptionAtomic.get()
        if (exception != null) {
            Throwables.throwIfInstanceOf(exception, Exception::class.java)
            throwIfUnchecked(exception)
            throw RuntimeException(exception)
        }
        return retAtomic.get()
    }

    companion object {
        private val TAG = EspressoTestRule::class.java.simpleName

        fun getActivitiesInStages(vararg stages: Stage): MutableSet<Activity> {
            val activities = Sets.newHashSet<Activity>()
            val instance = ActivityLifecycleMonitorRegistry.getInstance()
            for (stage in stages) {
                val activitiesInStage = instance.getActivitiesInStage(stage)
                if (activitiesInStage != null) {
                    activities.addAll(activitiesInStage)
                }
            }
            return activities
        }
    }
}
