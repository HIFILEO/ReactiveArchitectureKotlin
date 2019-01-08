package com.example.reactivearchitecture.nowplaying.viewmodel

import android.app.Application

import com.example.reactivearchitecture.categories.UnitTest
import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.interactor.NowPlayingInteractor
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfoImpl
import com.example.reactivearchitecture.nowplaying.model.uimodel.UiModel
import com.example.reactivearchitecture.core.model.action.Action
import com.example.reactivearchitecture.nowplaying.model.AdapterCommand
import com.example.reactivearchitecture.nowplaying.model.FilterManager
import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult
import com.example.reactivearchitecture.nowplaying.model.result.Result
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult
import com.example.reactivearchitecture.rx.RxJavaTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.whenever

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock

import java.util.ArrayList
import java.util.Date

import io.reactivex.Observable
import io.reactivex.observers.TestObserver

import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentMatchers.anyBoolean

import org.mockito.Mockito.verify

import org.mockito.MockitoAnnotations.initMocks

@Category(UnitTest::class)
class NowPlayingViewModelTest : RxJavaTest() {
    @Mock lateinit var mockServiceController: ServiceController

    @Mock lateinit var mockApplication: Application

    @Mock lateinit var mockNowPlayingInteractor: NowPlayingInteractor

    @Mock lateinit var mockFilterManager: FilterManager

    @Mock lateinit var mockTestTransformer: TestTransformer

    internal var movieInfo: MovieInfo = MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            Date(),
            9.0)

    internal var movieInfoLowRating: MovieInfo = MovieInfoImpl(
            "www.url_low.com",
            "Dan IS STILL The Man",
            Date(),
            5.0)

    @Before
    override fun setUp() {
        super.setUp()
        initMocks(this)

        whenever(mockNowPlayingInteractor.processAction(anyOrNull<Observable<Action>>()))
                .thenAnswer { invocation: InvocationOnMock ->

                    val actionObservable = invocation.arguments[0] as Observable<Action>

                    actionObservable.flatMap { action -> mockTestTransformer.transform(action) }
                }
    }

    @Test
    fun initState() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(
                mockApplication,
                mockServiceController,
                mockNowPlayingInteractor,
                mockFilterManager
        )
        nowPlayingViewModel.init(null)

        whenever(mockTestTransformer.transform(any<Action>())).thenReturn(Observable.empty())

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val uiModel = testObserver.events[0][0] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isEmpty()
        assertThat(uiModel.resultList).isEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(0)
    }

    @Test
    fun inFlightState() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(
                mockApplication,
                mockServiceController,
                mockNowPlayingInteractor,
                mockFilterManager)
        nowPlayingViewModel.init(null)

        val pageNumber = 1
        val scrollResult = ScrollResult.inFlight(pageNumber)

        // Note - replace Mockito ArgumentCaptor with inline Mockito-Kotlin
        val argumentCaptor = argumentCaptor<Action>()
        whenever(mockTestTransformer.transform(argumentCaptor.capture()))
                .thenReturn(Observable.just(scrollResult as Result))

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        // FilterManager Test (Must call Filter Manager)
        verify<FilterManager>(mockFilterManager).isFilterOn = anyBoolean()

        // Observer Test
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)

        // Model Test
        val uiModel = testObserver.events[0][1] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isEmpty()
        assertThat(uiModel.resultList).isEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(pageNumber)

        // Action translation test
        val action = argumentCaptor.firstValue
        assertThat(action).isNotNull()
        assertThat(action).isInstanceOf(ScrollAction::class.java)

        val scrollAction = action as ScrollAction
        assertThat(scrollAction.pageNumber).isEqualTo(pageNumber)
    }

    @Test
    fun inSuccessState() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(
                mockApplication,
                mockServiceController,
                mockNowPlayingInteractor,
                mockFilterManager
        )
        nowPlayingViewModel.init(null)

        val pageNumber = 1

        val scrollResultInFlight = ScrollResult.inFlight(pageNumber)

        val movieInfoList = ArrayList<MovieInfo>()
        movieInfoList.add(movieInfo)

        val scrollResultSuccess = ScrollResult.success(pageNumber, movieInfoList)

        val argumentCaptor = argumentCaptor<Action>()

        whenever(mockTestTransformer.transform(argumentCaptor.capture()))
                .thenReturn(Observable.just(
                        scrollResultInFlight as Result,
                        scrollResultSuccess as Result
                ))

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        // FilterManager Test (Must call Filter Manager)
        verify<FilterManager>(mockFilterManager).isFilterOn = anyBoolean()

        // Observer Test
        testObserver.assertNoErrors()
        testObserver.assertValueCount(3)

        // Model Test
        val uiModel = testObserver.events[0][2] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isFalse()
        assertThat(uiModel.adapterCommandType).isEqualTo(
                AdapterCommand.ADD_DATA_REMOVE_IN_PROGRESS)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(1)
        assertThat(uiModel.resultList).isNotEmpty
        assertThat(uiModel.resultList).hasSize(1)
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isTrue()
        assertThat(uiModel.pageNumber).isEqualTo(pageNumber)

        // Test List Data
        val movieViewInfo: MovieViewInfo = uiModel.resultList[0]
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo.isHighRating).isTrue()
    }

    @Test
    fun inRestoreState() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(
                mockApplication,
                mockServiceController,
                mockNowPlayingInteractor,
                mockFilterManager
        )

        // restore activity
        val pageNumber = 2

        val uiModelBuilder = UiModel.UiModelBuilder(UiModel.initState())
        uiModelBuilder.setPageNumber(pageNumber)

        val restoreState = uiModelBuilder.createUiModel()
        nowPlayingViewModel.init(restoreState)

        // Fake Data from Restore
        val movieInfoList1 = ArrayList<MovieInfo>()
        movieInfoList1.add(movieInfo)
        val movieInfoList2 = ArrayList<MovieInfo>()
        movieInfoList2.add(movieInfo)

        val restoreResult_inFlight_1 = RestoreResult.inFlight(1, null)
        val restoreResult_inFlight_1_success = RestoreResult.inFlight(1, movieInfoList1)
        val restoreResult_inFlight_2 = RestoreResult.inFlight(2, null)
        val restoreResult_success_2 = RestoreResult.success(2, movieInfoList2)

        val argumentCaptor = argumentCaptor<Action>()
        whenever(mockTestTransformer.transform(argumentCaptor.capture()))
                .thenReturn(Observable.just(
                        restoreResult_inFlight_1 as Result,
                        restoreResult_inFlight_1_success as Result,
                        restoreResult_inFlight_2 as Result,
                        restoreResult_success_2 as Result
                ))

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        // FilterManager Test (Must call Filter Manager)
        verify<FilterManager>(mockFilterManager).isFilterOn = anyBoolean()

        // Observer Test
        testObserver.assertNoErrors()
        testObserver.assertValueCount(5)

        // Model Test 1st Item
        var uiModel = testObserver.events[0][0] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isEmpty()
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(2)

        // Model Test 2nd Item
        uiModel = testObserver.events[0][1] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isEmpty()
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(1)

        // Model Test 3rd Item
        uiModel = testObserver.events[0][2] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.ADD_DATA_ONLY)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(1)
        assertThat(uiModel.resultList).isNotEmpty
        assertThat(uiModel.resultList).hasSize(1)
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(1)

        var movieViewInfo: MovieViewInfo? = uiModel.resultList[0]
        assertThat(movieViewInfo?.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo?.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo?.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo?.isHighRating).isTrue()

        // Model Test 4th Item
        uiModel = testObserver.events[0][3] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isTrue()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(1)
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isFalse()
        assertThat(uiModel.pageNumber).isEqualTo(pageNumber)

        // Model Test 5th Item
        uiModel = testObserver.events[0][4] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isFalse()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.ADD_DATA_ONLY)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(2)
        assertThat(uiModel.resultList).isNotEmpty
        assertThat(uiModel.resultList).hasSize(1)
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isTrue()
        assertThat(uiModel.pageNumber).isEqualTo(pageNumber)

        // test result
        movieViewInfo = uiModel.resultList[0]
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo.isHighRating).isTrue()

        // test full list
        movieViewInfo = uiModel.currentList[0]
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo.isHighRating).isTrue()

        movieViewInfo = uiModel.currentList[1]
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo.isHighRating).isTrue()
    }

    @Test
    fun inFilterState() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(mockApplication,
                mockServiceController, mockNowPlayingInteractor, mockFilterManager)

        // restore activity
        val movieViewInfoList_HighRating = ArrayList<MovieViewInfo>()
        val movieInfoList_HighRating = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieViewInfoList_HighRating.add(MovieViewInfoImpl(movieInfo))
            movieInfoList_HighRating.add(movieInfo)
        }
        val movieViewInfoList_LowRating = ArrayList<MovieViewInfo>()
        for (i in 0..4) {
            movieViewInfoList_LowRating.add(MovieViewInfoImpl(movieInfoLowRating))
        }
        val movieViewInfoList = ArrayList<MovieViewInfo>()
        movieViewInfoList.addAll(movieViewInfoList_LowRating)
        movieViewInfoList.addAll(movieViewInfoList_HighRating)

        val uiModelBuilder = UiModel.UiModelBuilder(UiModel.initState())
        uiModelBuilder.setPageNumber(2)
        uiModelBuilder.setCurrentList(movieViewInfoList)
        uiModelBuilder.setFilterOn(false)
        uiModelBuilder.setEnableScrollListener(true)
        uiModelBuilder.setFirstTimeLoad(false)
        uiModelBuilder.setResultList(emptyList())
        uiModelBuilder.setAdapterCommandType(AdapterCommand.DO_NOTHING)

        nowPlayingViewModel.init(uiModelBuilder.createUiModel())

        // Fake Data from FilterResult
        val filterResult = FilterResult.success(true, movieInfoList_HighRating)

        val argumentCaptor = argumentCaptor<Action>()
        whenever(mockTestTransformer.transform(argumentCaptor.capture()))
                .thenReturn(Observable.just(filterResult as Result))

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)

        // Model Test 1st Item
        var uiModel = testObserver.events[0][0] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isFalse()
        assertThat(uiModel.adapterCommandType).isEqualTo(AdapterCommand.DO_NOTHING)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(movieViewInfoList.size)
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isTrue()
        assertThat(uiModel.pageNumber).isEqualTo(2)

        // Model Test 2nd Item
        uiModel = testObserver.events[0][1] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isFalse()
        assertThat(uiModel.adapterCommandType).isEqualTo(
                AdapterCommand.SWAP_LIST_DUE_TO_NEW_FILTER)
        assertThat(uiModel.currentList).hasSize(movieInfoList_HighRating.size)
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isTrue()
        assertThat(uiModel.pageNumber).isEqualTo(2)

        // check values
        val movieViewInfo = uiModel.currentList!![0]
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase(Math.round(movieInfo.rating)
                .toString() + "/10")
        assertThat(movieViewInfo.isHighRating).isTrue()
    }

    @Test
    fun filterStateOnToOff() {
        //
        // Arrange
        //
        val testObserver: TestObserver<UiModel>
        val nowPlayingViewModel = TestNowPlayingViewModel(mockApplication,
                mockServiceController, mockNowPlayingInteractor, mockFilterManager)

        // restore activity
        val movieViewInfoList_HighRating = ArrayList<MovieViewInfo>()
        val movieInfoList_HighRating = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieViewInfoList_HighRating.add(MovieViewInfoImpl(movieInfo))
            movieInfoList_HighRating.add(movieInfo)
        }

        val movieViewInfoList_LowRating = ArrayList<MovieViewInfo>()
        val movieInfoList_LowRating = ArrayList<MovieInfo>()
        for (i in 0..4) {
            movieViewInfoList_LowRating.add(MovieViewInfoImpl(movieInfoLowRating))
            movieInfoList_LowRating.add(movieInfoLowRating)
        }

        val movieViewInfoList = ArrayList<MovieViewInfo>()
        movieViewInfoList.addAll(movieViewInfoList_LowRating)
        movieViewInfoList.addAll(movieViewInfoList_HighRating)

        val movieInfoList = ArrayList<MovieInfo>()
        movieInfoList.addAll(movieInfoList_HighRating)
        movieInfoList.addAll(movieInfoList_LowRating)

        val uiModelBuilder = UiModel.UiModelBuilder(UiModel.initState())
        uiModelBuilder.setPageNumber(2)
        uiModelBuilder.setCurrentList(movieViewInfoList)
        uiModelBuilder.setFilterOn(false)
        uiModelBuilder.setEnableScrollListener(true)
        uiModelBuilder.setFirstTimeLoad(false)
        uiModelBuilder.setResultList(emptyList())
        uiModelBuilder.setAdapterCommandType(AdapterCommand.DO_NOTHING)

        nowPlayingViewModel.init(uiModelBuilder.createUiModel())

        // Fake Data from FilterResult
        val filterResultOn = FilterResult.success(true, movieInfoList_HighRating)
        val filterResultOff = FilterResult.success(false, movieInfoList)

        val argumentCaptor = argumentCaptor<Action>()
        whenever(mockTestTransformer.transform(argumentCaptor.capture())).thenReturn(
                Observable.just(
                filterResultOn as Result,
                filterResultOff as Result))

        //
        // Act
        //
        testObserver = nowPlayingViewModel.uiModels!!.test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertNoErrors()
        testObserver.assertValueCount(3)

        // Model Test 3rd Item
        val uiModel = testObserver.events[0][2] as UiModel
        assertThat(uiModel).isNotNull()
        assertThat(uiModel.isFirstTimeLoad).isFalse()
        assertThat(uiModel.adapterCommandType).isEqualTo(
                AdapterCommand.SWAP_LIST_DUE_TO_NEW_FILTER)
        assertThat(uiModel.currentList).isNotEmpty
        assertThat(uiModel.currentList).hasSize(movieViewInfoList.size)
        assertThat(uiModel.resultList).isNullOrEmpty()
        assertThat(uiModel.failureMsg).isNull()
        assertThat(uiModel.isEnableScrollListener).isTrue()
        assertThat(uiModel.pageNumber).isEqualTo(2)
    }

    inner class TestTransformer {
        internal fun transform(action: Action): Observable<Result> {
            return Observable.empty()
        }
    }
}