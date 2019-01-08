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

package com.example.reactivearchitecture.nowplaying.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.annotation.VisibleForTesting
import android.support.annotation.WorkerThread

import com.example.reactivearchitecture.R
import com.example.reactivearchitecture.core.model.action.Action
import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.interactor.NowPlayingInteractor
import com.example.reactivearchitecture.nowplaying.model.AdapterCommand
import com.example.reactivearchitecture.nowplaying.model.FilterManager
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer
import com.example.reactivearchitecture.nowplaying.model.MovieInfo


import com.example.reactivearchitecture.nowplaying.model.action.FilterAction
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction
import com.example.reactivearchitecture.nowplaying.model.event.FilterEvent
import com.example.reactivearchitecture.nowplaying.model.event.RestoreEvent
import com.example.reactivearchitecture.nowplaying.model.event.ScrollEvent
import com.example.reactivearchitecture.nowplaying.model.event.UiEvent
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult
import com.example.reactivearchitecture.nowplaying.model.result.Result
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult
import com.example.reactivearchitecture.nowplaying.model.uimodel.UiModel
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfoImpl
import com.jakewharton.rxrelay2.PublishRelay

import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * View interface to be implemented by the forward facing UI part of android. Activity or fragment.
 *
 * @constructor - injected members
 * @param application
 * @param serviceController
 */
@SuppressLint("WrongConstant")
open class NowPlayingViewModel @Inject constructor(
        private val application: Application,
        private val serviceController: ServiceController
) : ViewModel() {
    var uiModels: Observable<UiModel>? = null
        private set
    private var initialUiModel: UiModel? = null
    private var startEventsObservable: Observable<UiEvent>? = null
    private var transformEventsIntoActions: ObservableTransformer<UiEvent, Action>

    //Note - left here to show example of android data binding.
    /**
     * Get the tool bar title.
     * @return - [ObservableField] that contains string for toolbar.
     */
    val toolbarTitle = ObservableField<String>()

    private val publishRelayUiEvents = PublishRelay.create<UiEvent>()

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    protected var nowPlayingInteractor: NowPlayingInteractor

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    protected var filterManager: FilterManager

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    protected var filterTransformer: FilterTransformer

    init {
        toolbarTitle.set(application.getString(R.string.now_playing))

        //
        // Create non injected data
        //
        filterManager = FilterManager(false)
        filterTransformer = FilterTransformer(filterManager)
        nowPlayingInteractor = NowPlayingInteractor(serviceController, filterTransformer)

        //
        // Setup Transformers
        //
        val scrollTransformer: ObservableTransformer<ScrollEvent, ScrollAction> =
            ObservableTransformer { upstream ->
                upstream.flatMap{ scrollEvent: ScrollEvent ->
                    Observable.just(ScrollAction(scrollEvent.pageNumber))
                }
            }

        val restoreTransformer: ObservableTransformer<RestoreEvent, RestoreAction> =
             ObservableTransformer { upstream ->
                 upstream.flatMap{ restoreEvent: RestoreEvent ->
                     Observable.just(RestoreAction(restoreEvent.pageNumber))
                 }
             }

        val filterTransformer: ObservableTransformer<FilterEvent, FilterAction> =
            ObservableTransformer { upstream ->
                upstream.flatMap{
                    filterEvent -> Observable.just(FilterAction(filterEvent.isFilterOn))
                }
            }

        transformEventsIntoActions = ObservableTransformer { upstream ->
            upstream.publish{ uiEventObservable ->
                Observable.merge<Action>(
                   uiEventObservable.ofType(ScrollEvent::class.java).compose(scrollTransformer),
                   uiEventObservable.ofType(RestoreEvent::class.java).compose(restoreTransformer),
                   uiEventObservable.ofType(FilterEvent::class.java).compose(filterTransformer)
                )
            }
        }
    }

    /**
     * Init the view model using the last saved UiModel.
     *
     * Can only be called once. Must be called before [NowPlayingViewModel.processUiEvent].
     *
     * @param restoredUiModel - model to restore, or null.
     */
    fun init(restoredUiModel: UiModel?) {
        if (initialUiModel == null) {
            if (restoredUiModel == null) {
                initialUiModel = UiModel.initState()
                startEventsObservable = Observable.just(
                        ScrollEvent((initialUiModel?.pageNumber ?: 0) + 1) as UiEvent
                )
            } else {
                //restore required
                val uiModelBuilder = UiModel.UiModelBuilder(restoredUiModel)
                initialUiModel = uiModelBuilder.createUiModel()
                startEventsObservable = Observable.just(
                        RestoreEvent(initialUiModel?.pageNumber ?: 0) as UiEvent
                )
            }
            filterManager.isFilterOn = initialUiModel?.isFilterOn ?: false
            bind()
        }
    }

    /**
     * Process events from the UI.
     * @param uiEvent - [UiEvent]
     */
    fun processUiEvent(uiEvent: UiEvent) {
        //
        //Guard
        //
        if (uiModels == null) {
            throw IllegalStateException("Model Observer not ready. Did you forget to call init()?")
        }

        //
        //Process UiEvent
        //
        Timber.i("Thread name: %s. Process UiEvent", Thread.currentThread().name)
        publishRelayUiEvents.accept(uiEvent)
    }

    /**
     * Bind to [PublishRelay].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    protected fun bind() {
        uiModels = publishRelayUiEvents
                .observeOn(Schedulers.computation())
                //Merge with start events
                .mergeWith(startEventsObservable!!)
                //Translate UiEvents into Actions
                .compose(transformEventsIntoActions)
                //Asynchronous Actions To Interactor
                .publish { actionObservable -> nowPlayingInteractor.processAction(actionObservable) }
                //Scan Results to Update UiModel
                .scan(initialUiModel!!) { uiModel: UiModel, result: Result ->
                    Timber.i(
                        "Thread name: %s. Scan Results to UiModel",
                        Thread.currentThread().name
                    )

                    when (result) {
                        is ScrollResult ->  return@scan processScrollResult(uiModel, result)
                        is RestoreResult -> return@scan processRestoreResult(uiModel, result)
                        is FilterResult -> return@scan processFilterResult(uiModel, result)
                        else -> {
                            //Unknown result - throw error
                            throw IllegalArgumentException("Unknown Result: $result")
                        }
                    }
                }
                //Publish results to main thread.
                .observeOn(AndroidSchedulers.mainThread())
                //Save history for late subscribers.
                .replay(1)
                .autoConnect()
    }

    /**
     * Translate internal business logic to presenter logic.
     * @param movieInfoList - business list.
     * @return - translated list ready for UI
     */
    @WorkerThread
    private fun translateResultsForUi(movieInfoList: List<MovieInfo>): List<MovieViewInfo> {
        val movieViewInfoList = ArrayList<MovieViewInfo>()
        for (movieInfo in movieInfoList) {
            movieViewInfoList.add(MovieViewInfoImpl(movieInfo))
        }

        return movieViewInfoList
    }

    /**
     * Update the [UiModel] based on input form a [ScrollResult].
     * @param uiModel - model to update
     * @param scrollResult - results from [ScrollAction]
     * @return new updated [UiModel]
     */
    private fun processScrollResult(uiModel: UiModel, scrollResult: ScrollResult): UiModel {
        val uiModelBuilder = UiModel.UiModelBuilder(uiModel)

        when (scrollResult.type) {
            Result.IN_FLIGHT ->
                //In Progress
                uiModelBuilder
                        .setFirstTimeLoad(scrollResult.pageNumber == 1)
                        .setFailureMsg(null)
                        .setPageNumber(scrollResult.pageNumber)
                        .setEnableScrollListener(false)
                        .setResultList(emptyList())
                        .setAdapterCommandType(if (scrollResult.pageNumber == 1)
                            AdapterCommand.DO_NOTHING
                        else
                            AdapterCommand.SHOW_IN_PROGRESS)
            Result.SUCCESS -> {
                val listToAdd = translateResultsForUi(scrollResult.result!!)
                val currentList: MutableList<MovieViewInfo> = uiModel.currentList.toMutableList()
                currentList.addAll(listToAdd)

                //Success
                uiModelBuilder
                        .setFirstTimeLoad(false)
                        .setFailureMsg(null)
                        .setPageNumber(scrollResult.pageNumber)
                        .setEnableScrollListener(true)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(AdapterCommand.ADD_DATA_REMOVE_IN_PROGRESS)
            }
            Result.FAILURE -> {
                Timber.e(scrollResult.error)

                //Failure
                uiModelBuilder
                        .setFirstTimeLoad(scrollResult.pageNumber == 1)
                        .setFailureMsg(application.getString(R.string.error_msg))
                        .setPageNumber(scrollResult.pageNumber - 1)
                        .setEnableScrollListener(false)
                        .setResultList(emptyList())
                        .setAdapterCommandType(AdapterCommand.DO_NOTHING)
            }
            else ->
                //Unknown result - throw error
                throw IllegalArgumentException("Unknown ResultType: " + scrollResult.type)
        }

        return uiModelBuilder.createUiModel()
    }

    /**
     * Update the [UiModel] based on input form a [RestoreResult].
     * @param uiModel - model to update
     * @param restoreResult - results from [RestoreAction]
     * @return new updated [UiModel]
     */
    private fun processRestoreResult(uiModel: UiModel, restoreResult: RestoreResult): UiModel {
        val uiModelBuilder = UiModel.UiModelBuilder(uiModel)

        var listToAdd: List<MovieViewInfo> = emptyList()
        val currentList: MutableList<MovieViewInfo> = uiModel.currentList.toMutableList()

        if (restoreResult.result != null && !restoreResult.result.isEmpty()) {
            listToAdd = translateResultsForUi(restoreResult.result)
            currentList.addAll(listToAdd)
        }

        when (restoreResult.type) {
            Result.IN_FLIGHT ->
                //In Progress
                uiModelBuilder
                        .setFirstTimeLoad(true)
                        .setFailureMsg(null)
                        .setPageNumber(restoreResult.pageNumber)
                        .setEnableScrollListener(false)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(if (listToAdd.isEmpty())
                            AdapterCommand.DO_NOTHING
                        else
                            AdapterCommand.ADD_DATA_ONLY)
            Result.SUCCESS ->
                //Success
                uiModelBuilder
                        .setFirstTimeLoad(false)
                        .setFailureMsg(null)
                        .setPageNumber(restoreResult.pageNumber)
                        .setEnableScrollListener(true)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(AdapterCommand.ADD_DATA_ONLY)
            Result.FAILURE -> {
                Timber.e(restoreResult.error)

                //Error
                uiModelBuilder
                        .setFirstTimeLoad(true)
                        .setFailureMsg(application.getString(R.string.error_msg))
                        .setPageNumber(restoreResult.pageNumber - 1)
                        .setEnableScrollListener(false)
                        .setResultList(emptyList())
                        .setAdapterCommandType(AdapterCommand.DO_NOTHING)
            }
            else ->
                //Unknown result - throw error
                throw IllegalArgumentException("Unknown ResultType: " + restoreResult.type)
        }

        return uiModelBuilder.createUiModel()
    }

    /**
     * Update the [UiModel] based on input form a [FilterResult].
     * @param uiModel - model to udate
     * @param filterResult - results from [FilterResult]
     * @return new updated [UiModel]
     */
    private fun processFilterResult(uiModel: UiModel, filterResult: FilterResult): UiModel {
        val uiModelBuilder = UiModel.UiModelBuilder(uiModel)

        when (filterResult.type) {
            Result.IN_FLIGHT -> Timber.i("Filter - IN_FLIGHT")
            Result.SUCCESS ->
                //Success
                uiModelBuilder
                        .setCurrentList(translateResultsForUi(filterResult.filteredList!!))
                        .setFilterOn(filterResult.isFilterOn)
                        .setResultList(emptyList())
                        .setAdapterCommandType(AdapterCommand.SWAP_LIST_DUE_TO_NEW_FILTER)
            Result.FAILURE -> {
                Timber.e("Failure during filter. Throw error, this should never happen.")
                throw IllegalArgumentException("Failure during filter. This should never happen.")
            }
            else ->
                //Unknown result - throw error
                throw IllegalArgumentException("Unknown ResultType: " + filterResult.type)
        }

        return uiModelBuilder.createUiModel()
    }
}
