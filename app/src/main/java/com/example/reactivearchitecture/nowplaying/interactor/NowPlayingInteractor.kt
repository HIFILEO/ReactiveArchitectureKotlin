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

import android.support.annotation.VisibleForTesting

import com.example.reactivearchitecture.core.model.action.Action
import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer
import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo
import com.example.reactivearchitecture.nowplaying.model.action.FilterAction
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult
import com.example.reactivearchitecture.nowplaying.model.result.Result
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult

import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * Interactor for Now Playing movies. Handles internal business logic interactions.
 */
class NowPlayingInteractor(
    private val serviceController: ServiceController,
    private val filterTransformer: FilterTransformer
) {

    private val transformScrollActionToScrollResult:
        ObservableTransformer<ScrollAction, ScrollResult> = ObservableTransformer { upstream ->
            Timber.i(
                "Thread name: %s. Translate ScrollAction into ScrollResult.",
                Thread.currentThread().name
            )

            upstream.flatMap { scrollAction: ScrollAction ->
                Timber.i(
                    "Thread name: %s. Load Data, return ScrollResult.",
                    Thread.currentThread().name
                )
                val failureStream: PublishSubject<ScrollResult> = PublishSubject.create()

                serviceController.getNowPlaying(scrollAction.pageNumber)
                        // Delay for 3 seconds to show spinner on screen.
                        .delay(3, TimeUnit.SECONDS)
                        // translate external to internal business logic (Example if we wanted to save to prefs)
                        .flatMap(MovieListFetcher())
                        .flatMap { movieInfos: List<MovieInfo> ->
                            Observable.just(
                                    ScrollResult.success(
                                            scrollAction.pageNumber,
                                            movieInfos
                                    ))
                        }
                        .retry { retryNumber: Int, throwable: Throwable ->
                            failureStream.onNext(
                                    ScrollResult.failure(scrollAction.pageNumber, throwable))
                            true
                        }
                        .mergeWith(failureStream)
                        .startWith(ScrollResult.inFlight(scrollAction.pageNumber))
            }
        }

    private val transformRestoreActionToRestoreResult:
        ObservableTransformer<RestoreAction, RestoreResult> = ObservableTransformer { upstream ->
            Timber.i(
                    "Thread name: %s. Translate RestoreAction into RestoreResult.",
                    Thread.currentThread().name
            )

            upstream.flatMap { restoreAction: RestoreAction ->
                // Set the number of pages to restore
                val pagesToRestore = arrayListOf<Int>()
                for (i in 1..restoreAction.pageNumberToRestore) {
                    pagesToRestore.add(i)
                }

                val failureStream: PublishSubject<RestoreResult> = PublishSubject.create()

                // Execute
                Observable.fromIterable(pagesToRestore)
                        .concatMap { pageNumber: Int ->
                            Timber.i(
                                    "Thread name: %s. Restore Page #%s.",
                                    Thread.currentThread().name, pageNumber
                            )

                            serviceController.getNowPlaying(pageNumber = pageNumber)
                                    .delay(3, TimeUnit.SECONDS)
                                    // translate external to internal business logic (Ex if we wanted to save to prefs)
                                    .flatMap(MovieListFetcher())
                                    .flatMap { movieInfos: List<MovieInfo> ->
                                        Timber.i(
                                            "Thread name: %s. Create Restore Results for page %s",
                                            Thread.currentThread().getName(), pageNumber
                                        )
                                        if (pageNumber == restoreAction.pageNumberToRestore) {
                                            Observable.just(
                                                    RestoreResult.success(pageNumber, movieInfos))
                                        } else {
                                            Observable.just(
                                                    RestoreResult.inFlight(pageNumber, movieInfos))
                                        }
                                    }
                                    // handle retry, send status on failure stream.
                                    .retry { retryNumber: Int, throwable: Throwable ->
                                        failureStream.onNext(RestoreResult.failure(
                                                restoreAction.pageNumberToRestore,
                                                true,
                                                throwable
                                        ))
                                        true
                                    }
                                    .startWith(RestoreResult.inFlight(pageNumber, null))
                        }
                        .mergeWith(failureStream)
            }
        }

    private val transformActionIntoResults: ObservableTransformer<Action, Result> =
        ObservableTransformer { actionObservable: Observable<Action> ->
            Timber.i(
                    "Thread name: %s. Translate Actions into Specific Actions.",
                    Thread.currentThread().name
            )

            Observable.merge(
                    actionObservable.ofType(FilterAction::class.java),
                    actionObservable.ofType(ScrollAction::class.java)
                            .compose(transformScrollActionToScrollResult),
                    actionObservable.ofType(RestoreAction::class.java)
                            .compose(transformRestoreActionToRestoreResult))
                    .concatMap { theObject -> processFiltering(theObject as Object) }
        }

    /**
     * Process {@link Action}.
     * @param actions - action to process.
     * @return - {@link Result} of the asynchronous event.
     */
    fun processAction(actions: Observable<Action>): Observable<Result> {
        return actions.compose(transformActionIntoResults)
    }

    /**
     * Process Filtering for {@link ScrollResult}, {@link RestoreResult}, {@link FilterAction}.
     * @param object - object to apply filtering on.
     * @return {@link Observable} of {@link Result}
     */
    private fun processFiltering(theObject: Object): Observable<Result> {
        return Observable.just(theObject)
                .publish { objectObservable: Observable<Object> ->
                    Observable.merge(
                            objectObservable.ofType(FilterAction::class.java)
                                    .compose(filterTransformer.transformFilterActionToFilterResult),
                            objectObservable.ofType(ScrollResult::class.java)
                                    .compose(filterTransformer.transformFilterScrollResult),
                            objectObservable.ofType(RestoreResult::class.java)
                                    .compose(filterTransformer.transformFilterRestoreResult))
                }
    }
}

/**
 * Fetch movies list from [NowPlayingInfo].
 */
@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
class MovieListFetcher : Function<NowPlayingInfo, ObservableSource<List<MovieInfo>>> {

    @Throws(Exception::class)
    override fun apply(@io.reactivex.annotations.NonNull nowPlayingInfo: NowPlayingInfo):
            ObservableSource<List<MovieInfo>> {
        Timber.i(
                "Thread name: %s. Translate External Api Data into Business Internal " +
                        "Business Logic Data.",
                Thread.currentThread().name
        )
        return Observable.just(nowPlayingInfo.movies)
    }
}