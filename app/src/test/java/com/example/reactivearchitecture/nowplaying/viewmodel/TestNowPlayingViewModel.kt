package com.example.reactivearchitecture.nowplaying.viewmodel

import android.app.Application

import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.interactor.NowPlayingInteractor
import com.example.reactivearchitecture.nowplaying.model.FilterManager

/**
 * Test class to override the created objects during construction that we don't want passed in via dagger.
 * @param application
 * @param serviceController
 * @param nowPlayingInteractor
 * @param filterManager
 */
class TestNowPlayingViewModel(
    application: Application,
    serviceController: ServiceController,
    nowPlayingInteractor: NowPlayingInteractor,
    filterManager: FilterManager
) : NowPlayingViewModel(application, serviceController) {

    init {
        super.nowPlayingInteractor = nowPlayingInteractor
        super.filterManager = filterManager
    }
}
