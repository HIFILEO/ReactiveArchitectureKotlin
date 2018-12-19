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

package com.example.reactivearchitecture.nowplaying.model.result

import android.support.annotation.IntDef

/**
 * Base class for results from asynchronous actions.
 *
 * Note - IntDef in Kotlin not easy
 * https://medium.com/@xzan/the-drawbacks-of-migrating-to-kotlin-51f49a96107a
 */
abstract class Result {

    @get:ResultType
    abstract val type: Int

    /**
     * Kotlin IntDef
     */
    @IntDef(IN_FLIGHT, SUCCESS, FAILURE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ResultType

    companion object {
        const val IN_FLIGHT = 0
        const val SUCCESS = 1
        const val FAILURE = 2
    }
}
