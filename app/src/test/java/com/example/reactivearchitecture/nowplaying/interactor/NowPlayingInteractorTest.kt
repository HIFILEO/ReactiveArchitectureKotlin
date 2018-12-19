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

package com.example.reactivearchitecture.nowplaying.interactor

import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.model.FilterManager
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer
import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfoImpl
import com.example.reactivearchitecture.core.model.action.Action
import com.example.reactivearchitecture.nowplaying.model.action.FilterAction
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult
import com.example.reactivearchitecture.nowplaying.model.result.Result
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult
import com.example.reactivearchitecture.rx.RxJavaTest
import com.nhaarman.mockitokotlin2.whenever

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock

import java.util.ArrayList
import java.util.Date
import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.observers.TestObserver

import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList

import org.mockito.MockitoAnnotations.initMocks

/**
 * Test the business logic from the interactor.
 */
class NowPlayingInteractorTest : RxJavaTest() {

    @Mock lateinit var mockServiceController: ServiceController

    @Mock lateinit var mockFilterManager: FilterManager

    lateinit var filterTransformer: FilterTransformer

    internal val movieInfo: MovieInfo = MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            Date(),
            9.0)

    internal val pageNumber = 1
    internal val totalPageNumber = 10

    @Before
    override fun setUp() {
        super.setUp()
        initMocks(this)

        whenever(mockFilterManager.filterList(anyList())).thenAnswer {
            invocation: InvocationOnMock -> invocation.arguments[0]
        }

        // Use real transformers.
        filterTransformer = FilterTransformer(mockFilterManager)
    }

    @Test
    fun testScrollAction_pass() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber)

        whenever(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo))

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(ScrollAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)

        // IN_FLIGHT Test
        val result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        val scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNull()
        assertThat(scrollResult.result).isNull()
        assertThat(scrollResult.type).isEqualTo(Result.IN_FLIGHT)

        // SUCCESS
        val resultSuccess = testObserver.events[0][1] as Result
        assertThat(resultSuccess).isNotNull()
        assertThat(resultSuccess).isInstanceOf(ScrollResult::class.java)

        val scrollResultSuccess = resultSuccess as ScrollResult
        assertThat(scrollResultSuccess.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResultSuccess.error).isNull()
        assertThat(scrollResultSuccess.result).isNotEmpty
        assertThat(scrollResultSuccess.result).hasSize(5)
        assertThat(scrollResultSuccess.type).isEqualTo(Result.SUCCESS)

        for (i in 0..4) {
            assertThat(scrollResultSuccess.result!![i]).isEqualTo(movieInfo)
        }
    }

    @Test
    fun testScrollAction_fail() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber, totalPageNumber)

        val errorMessage = "Error Message"

        val testFailure = TestFailure(nowPlayingInfo, errorMessage)
        whenever(mockServiceController.getNowPlaying(1))
                .thenReturn(testFailure.nowPlayingInfoObservable)

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(ScrollAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(3)

        // IN_FLIGHT Test
        var result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        var scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNull()
        assertThat(scrollResult.result).isNull()
        assertThat(scrollResult.type).isEqualTo(Result.IN_FLIGHT)

        // FAILURE
        result = testObserver.events[0][1] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNotNull()
        assertThat(scrollResult.error?.message).isEqualTo(errorMessage)
        assertThat(scrollResult.result).isNull()
        assertThat(scrollResult.type).isEqualTo(Result.FAILURE)

        // SUCCESS
        result = testObserver.events[0][2] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNull()
        assertThat(scrollResult.result).isNotEmpty
        assertThat(scrollResult.result).hasSize(5)
        assertThat(scrollResult.type).isEqualTo(Result.SUCCESS)

        for (i in 0..4) {
            assertThat(scrollResult.result!![i]).isEqualTo(movieInfo)
        }
    }

    @Test
    fun testResultAction_pass() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber)

        whenever(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo))

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(RestoreAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)

        // IN_FLIGHT Test
        var result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(RestoreResult::class.java)

        var restoreResult = result as RestoreResult
        assertThat(restoreResult.pageNumber).isEqualTo(pageNumber)
        assertThat(restoreResult.error).isNull()
        assertThat(restoreResult.result).isNull()
        assertThat(restoreResult.type).isEqualTo(Result.IN_FLIGHT)

        // SUCCESS
        result = testObserver.events[0][1] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(RestoreResult::class.java)

        restoreResult = result as RestoreResult
        assertThat(restoreResult.pageNumber).isEqualTo(pageNumber)
        assertThat(restoreResult.error).isNull()
        assertThat(restoreResult.result).isNotEmpty
        assertThat(restoreResult.result).hasSize(5)
        assertThat(restoreResult.type).isEqualTo(Result.SUCCESS)
    }

    @Test
    fun testResultAction_fail() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber, totalPageNumber)

        val errorMessage = "Error Message"

        val testFailure = TestFailure(nowPlayingInfo, errorMessage)
        whenever(mockServiceController.getNowPlaying(1))
                .thenReturn(testFailure.nowPlayingInfoObservable)

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(RestoreAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(3)

        // IN_FLIGHT Test
        var result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(RestoreResult::class.java)

        var restoreResult = result as RestoreResult
        assertThat(restoreResult.pageNumber).isEqualTo(pageNumber)
        assertThat(restoreResult.error).isNull()
        assertThat(restoreResult.result).isNull()
        assertThat(restoreResult.type).isEqualTo(Result.IN_FLIGHT)

        // FAILURE
        result = testObserver.events[0][1] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(RestoreResult::class.java)

        restoreResult = result as RestoreResult
        assertThat(restoreResult.pageNumber).isEqualTo(pageNumber)
        assertThat(restoreResult.error).isNotNull()
        assertThat(restoreResult.error?.message).isEqualTo(errorMessage)
        assertThat(restoreResult.result).isNull()
        assertThat(restoreResult.type).isEqualTo(Result.FAILURE)

        // SUCCESS
        result = testObserver.events[0][2] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(RestoreResult::class.java)

        restoreResult = result as RestoreResult
        assertThat(restoreResult.pageNumber).isEqualTo(pageNumber)
        assertThat(restoreResult.error).isNull()
        assertThat(restoreResult.result).isNotEmpty
        assertThat(restoreResult.result).hasSize(5)
        assertThat(restoreResult.type).isEqualTo(Result.SUCCESS)

        for (i in 0..4) {
            assertThat(restoreResult.result!![i]).isEqualTo(movieInfo)
        }
    }

    @Test
    fun testResultAction_pass_multiple_results() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val pageNumber = 2

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber)

        whenever(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo))

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(RestoreAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(4)
    }

    @Test
    fun testFilter_turnFilterOn() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }

        whenever(mockFilterManager.isFilterOn).thenReturn(true)
        whenever(mockFilterManager.fullList).thenReturn(movieInfoList)

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(FilterAction(true) as Action)).test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        // IN_FLIGHT Test
        val result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(FilterResult::class.java)

        val filterResult = result as FilterResult
        assertThat(filterResult.filteredList?.containsAll(movieInfoList))
        assertThat(filterResult.isFilterOn).isTrue()
        assertThat(filterResult.type).isEqualTo(Result.SUCCESS)
    }

    @Test
    fun testFilter_ScrollResults() {
        //
        // Arrange
        //
        val testObserver: TestObserver<Result>
        val nowPlayingInteractor = NowPlayingInteractor(mockServiceController, filterTransformer)

        val pageNumber = 2

        val movieInfoList = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieInfoList.add(movieInfo)
        }
        val nowPlayingInfo = NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber)

        whenever(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo))

        whenever(mockFilterManager.isFilterOn).thenReturn(true)
        whenever(mockFilterManager.filterList(anyList())).thenReturn(ArrayList())

        //
        // Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just(ScrollAction(pageNumber) as Action)).test()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)

        // IN_FLIGHT Test
        var result = testObserver.events[0][0] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        var scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNull()
        assertThat(scrollResult.result).isNull()
        assertThat(scrollResult.type).isEqualTo(Result.IN_FLIGHT)

        // SUCCESS
        result = testObserver.events[0][1] as Result
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(ScrollResult::class.java)

        scrollResult = result as ScrollResult
        assertThat(scrollResult.pageNumber).isEqualTo(pageNumber)
        assertThat(scrollResult.error).isNull()
        assertThat(scrollResult.result).isEmpty()
        assertThat(scrollResult.type).isEqualTo(Result.SUCCESS)
    }

    /**
     * Test class used to test Rx
     * [io.reactivex.internal.operators.observable.ObservableRetryPredicate].
     * @param nowPlayingInfo
     * @param errorMessage
     */
    private inner class TestFailure(
        private val nowPlayingInfo: NowPlayingInfo,
        private val errorMessage: String
    ) {
        private var didFailOnce = false

        /**
         * Use Mockito to trigger this during test.
         * @return - [Exception] first time it's called, [NowPlayingInfo] the second.
         */
        internal val nowPlayingInfoObservable: Observable<NowPlayingInfo>
            get() = Observable.fromCallable {
                if (didFailOnce) {
                    nowPlayingInfo
                } else {
                    didFailOnce = true
                    throw Exception(errorMessage)
                }
            }
    }
}