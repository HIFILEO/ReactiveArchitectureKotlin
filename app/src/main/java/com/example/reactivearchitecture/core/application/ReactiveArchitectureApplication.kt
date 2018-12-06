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

package com.example.reactivearchitecture.core.application

import android.app.Activity
import android.app.Application
import android.support.annotation.VisibleForTesting
import android.util.Log

import com.example.reactivearchitecture.BuildConfig
import com.example.reactivearchitecture.core.dagger.AppInjector
import com.example.reactivearchitecture.core.dagger.ApplicationComponent

import javax.inject.Inject

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector

import timber.log.Timber

/**
 * This is the application class for setting up Dagger 2 and Timber.
 */
open class ReactiveArchitectureApplication : Application(), HasActivityInjector {
    private var component: ApplicationComponent? = null

    @Inject
    internal lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        setupComponent()
        setupTimber()
    }

    /**
     * Setup the Timber logging tree.
     */
    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    /**
     * Setup the Dagger2 component graph. Must be called before [.onCreate]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal open fun setupComponent() {
        if (component == null) {
            component = AppInjector.init(this)
        } else {
            Log.d(ReactiveArchitectureApplication::class.java.simpleName,
                    "setupComponent() called.  " + "ApplicationComponent already set.")
        }
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingActivityInjector
    }
}
