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
import com.example.reactivearchitecture.nowplaying.model.MovieInfo

/**
 * Base class for results from asynchronous actions.
 *
 * Note - IntDef in Kotlin not easy
 * https://medium.com/@xzan/the-drawbacks-of-migrating-to-kotlin-51f49a96107a
 */
sealed class Result {

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

data class ScrollResult(
    @param:ResultType @field:ResultType @get:ResultType override val type: Int,
    val isSuccessful: Boolean,
    val isLoading: Boolean,
    val pageNumber: Int,
    val result: List<MovieInfo>,
    val error: Throwable? // TODO - Arrow?
) : Result() {

    /**
     * Note - Kotlin Companion Factory Method
     */
    companion object {

        fun inFlight(pageNumber: Int): ScrollResult {
            return ScrollResult(
                    Result.IN_FLIGHT,
                    false,
                    true,
                    pageNumber,
                    emptyList(),
                    null)
        }

        fun success(pageNumber: Int, result: List<MovieInfo>): ScrollResult {
            return ScrollResult(
                    Result.SUCCESS,
                    true,
                    false,
                    pageNumber,
                    result,
                    null
            )
        }

        fun failure(pageNumber: Int, error: Throwable): ScrollResult {
            return ScrollResult(
                    Result.FAILURE,
                    false,
                    false,
                    pageNumber,
                    emptyList(),
                    error
            )
        }
    }
}

data class FilterResult private constructor(
    @field:ResultType override val type: Int,
    val isFilterOn: Boolean,
    private val filterInProgress: Boolean,
    val filteredList: List<MovieInfo>?
) : Result() {

    /**
     * Note - These are CFM (Companion Facotry Methods) used instead of Java SFM
     * https://blog.kotlin-academy.com/effective-java-in-kotlin-item-1-consider-static-factory-methods-instead-of-constructors-8d0d7b5814b2
     */
    companion object {

        /**
         * In progress creator.
         * @param filterOn -
         * @return new [FilterResult]
         */
        fun inProgress(filterOn: Boolean): FilterResult {
            return FilterResult(
                    Result.IN_FLIGHT,
                    filterOn,
                    true,
                    null)
        }

        /**
         * Success Creator.
         * @param filterOn -
         * @param listToShow -
         * @return new [FilterResult]
         */
        fun success(filterOn: Boolean, listToShow: List<MovieInfo>): FilterResult {
            return FilterResult(
                    Result.SUCCESS,
                    filterOn,
                    true,
                    listToShow)
        }
    }
}

data class RestoreResult(
    @field:ResultType override val type: Int,
    val isSuccessful: Boolean,
    val isLoading: Boolean,
    val pageNumber: Int,
    val result: List<MovieInfo>?,
    val error: Throwable?
) : Result() {

    /**
     * Note - Kotlin CFM
     */
    companion object {

        fun inFlight(pageNumber: Int, result: List<MovieInfo>?): RestoreResult {
            return RestoreResult(
                    Result.IN_FLIGHT,
                    true,
                    true,
                    pageNumber,
                    result,
                    null
            )
        }

        fun success(pageNumber: Int, result: List<MovieInfo>): RestoreResult {
            return RestoreResult(
                    Result.SUCCESS,
                    true,
                    false,
                    pageNumber,
                    result,
                    null
            )
        }

        fun failure(pageNumber: Int, inProgress: Boolean, error: Throwable): RestoreResult {
            return RestoreResult(
                    Result.FAILURE,
                    false,
                    inProgress,
                    pageNumber,
                    null,
                    error)
        }
    }
}
