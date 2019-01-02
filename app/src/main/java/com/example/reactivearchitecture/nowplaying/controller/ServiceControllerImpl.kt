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

package com.example.reactivearchitecture.nowplaying.controller

import android.annotation.SuppressLint
import android.support.annotation.VisibleForTesting

import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfoImpl
import com.example.reactivearchitecture.nowplaying.service.ServiceApi
import com.example.reactivearchitecture.nowplaying.service.ServiceResponse

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Implementation of [ServiceController].
 *
 * @param serviceApi - Retrofit service.
 * @param apiKey - access key.
 * @param imageUrlPath - url base path for showing images.
 */
class ServiceControllerImpl(
    private val serviceApi: ServiceApi,
    private val apiKey: String,
    private val imageUrlPath: String
) : ServiceController {

    override fun getNowPlaying(pageNumber: Int): Observable<NowPlayingInfo> {
        Timber.i("Thread name: %s. Get NowPlaying for Page #%s.",
                Thread.currentThread().name, pageNumber)

        val mapToSend = HashMap<String, Int>()
        mapToSend["page"] = pageNumber

        /*
        Notes - Load data from web on scheduler thread. Translate the web response to our
        internal business response on computation thread. Return observable.
         */
        return serviceApi.nowPlaying(apiKey, mapToSend)
                // subscribe up - call api using io scheduler.
                .subscribeOn(Schedulers.io())
                // observe down - translate on computation scheduler.
                .observeOn(Schedulers.computation())
                .flatMap(TranslateNowPlayingSubscriptionFunc(imageUrlPath))
                .doOnError { throwable ->
                    Timber.e("Failed to get data from service. %s", throwable.toString())
                    throw Exception("Service failed to get data from API.")
                }
    }

    /**
     * Class to translate external [ServiceResponse] to internal data for [NowPlayingInfo].
     * @param imageUrlPath - base path to downloading images.
     */
    @VisibleForTesting
    internal class TranslateNowPlayingSubscriptionFunc(
        private val imageUrlPath: String
    ) : Function<ServiceResponse, ObservableSource<NowPlayingInfo>> {

        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        @Throws(Exception::class)
        override fun apply(@NonNull serviceResponse: ServiceResponse): Observable<NowPlayingInfo> {
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().name,
                    javaClass.simpleName)
            val movieInfoList = ArrayList<MovieInfo>()

            for (i in 0 until (serviceResponse.results?.size ?: 0)) {
                val movieInfo = MovieInfoImpl(
                        imageUrlPath + serviceResponse.results?.get(i)?.poster_path,
                        serviceResponse.results?.get(i)?.title ?: "",
                        dateFormat.parse(serviceResponse.results?.get(i)?.release_date),
                        serviceResponse.results?.get(i)?.vote_average ?: 0.toDouble())

                movieInfoList.add(movieInfo)
            }

            return Observable.just(NowPlayingInfoImpl(movieInfoList,
                    serviceResponse.page,
                    serviceResponse.total_pages) as NowPlayingInfo)
        }
    }
}
