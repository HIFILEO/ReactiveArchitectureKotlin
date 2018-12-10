package com.example.reactivearchitecture.core.dagger

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import com.example.reactivearchitecture.core.viewmodel.ReactiveArchitectureViewModelFactory
import com.example.reactivearchitecture.nowplaying.viewmodel.NowPlayingViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Module for ViewModels used throughout the [android.app.Application].
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(NowPlayingViewModel::class)
    internal abstract fun bindUserViewModel(nowPlayingViewModel: NowPlayingViewModel): ViewModel

    //  Note - The factory from Android that allows us to customize the constructor for ViewModels.
    //  Otherwise ViewModels will have no constructor.
    @Binds
    internal abstract fun bindViewModelFactory(factory: ReactiveArchitectureViewModelFactory):
            ViewModelProvider.Factory
}
