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

import android.support.test.espresso.idling.CountingIdlingResource

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import io.reactivex.internal.operators.observable.ObservableDelay

/**
 * Check observables that are triggered in Rx. For those that are Delays, you need to increment [CountingIdlingResource]
 * because [RxEspressoScheduleHandler] only tracks "running" resources.
 */
class RxEspressoObserverHandler internal constructor(
    private val countingIdlingResource: CountingIdlingResource
) : Function<Observable<*>, Observable<*>> {

    @Throws(Exception::class)
    override fun apply(@NonNull observable: Observable<*>): Observable<*> {
        /**
         * ObservableDelay need to count for idling.
         */
        if (observable is ObservableDelay<*>) {
            countingIdlingResource.increment()

            // Use reflection since ObservableDelay uses source to trigger actual scheduled wait.
            val delayPrivateField = ObservableDelay::class.java.getDeclaredField("delay")
            val timeUnitsPrivateField = ObservableDelay::class.java.getDeclaredField("unit")

            delayPrivateField.isAccessible = true
            timeUnitsPrivateField.isAccessible = true

            val delay = delayPrivateField.getLong(observable)
            val unit = timeUnitsPrivateField.get(observable) as TimeUnit

            // Create timer task to decrement delay.
            // Note - tack on 100ms for thread switching since this is started before subscription actually triggers.
            val timeTask = object : TimerTask() {
                override fun run() {
                    countingIdlingResource.decrement()
                }
            }
            val timer = Timer()
            val milliseconds = TimeUnit.MILLISECONDS.convert(delay, unit) + 100
            timer.schedule(timeTask, milliseconds)
        }

        return observable
    }
}
