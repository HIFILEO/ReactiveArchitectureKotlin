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

package com.example.reactivearchitecture.core.dagger

import android.app.Application

import com.example.reactivearchitecture.R
import com.example.reactivearchitecture.nowplaying.controller.ServiceController
import com.example.reactivearchitecture.nowplaying.controller.ServiceControllerImpl
import com.example.reactivearchitecture.nowplaying.service.ServiceApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import org.mockito.Mockito

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dagger2 [Module] providing application-level dependency bindings for test.
 */
@Module(includes = arrayOf(ViewModelModule::class))
class TestApplicationModule {

    @Singleton
    @Provides
    fun providesGson(builder: GsonBuilder): Gson {
        return builder.create()
    }

    @Provides
    fun providesGsonBuilder(): GsonBuilder {
        return GsonBuilder()
    }

    @Singleton
    @Provides
    fun providesOkHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(builder: OkHttpClient.Builder, level: HttpLoggingInterceptor.Level):
            OkHttpClient {
        // Log HTTP request and response data in debug mode
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = level
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }

    @Singleton
    @Provides
    fun providesRetrofitBuilder(client: OkHttpClient, gson: Gson): Retrofit.Builder {
        return Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptorLevel(): HttpLoggingInterceptor.Level {
        return HttpLoggingInterceptor.Level.NONE
    }

    @Provides
    @Singleton
    fun providesInfoServiceApi(retrofit: Retrofit.Builder): ServiceApi {
        // Note - mock the web calls. You test the in contract testing.
        return Mockito.mock(ServiceApi::class.java)
    }

    @Provides
    @Singleton
    fun providesGatewayInfo(serviceApi: ServiceApi, application: Application): ServiceController {
        return ServiceControllerImpl(serviceApi,
                application.getString(R.string.api_key),
                application.getString(R.string.image_url_path))
    }
}
