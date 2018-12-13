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

package com.example.reactivearchitecture.nowplaying.service

import com.example.reactivearchitecture.rx.RxJavaTest
import com.google.gson.Gson

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

import java.util.HashMap

import com.example.reactivearchitecture.categories.ContractTest

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import org.assertj.core.api.Assertions.assertThat
import org.mockito.MockitoAnnotations.initMocks

/**
 * Run [ServiceApi] Tests.
 */
@Category(ContractTest::class)
class ServiceApiTest : RxJavaTest() {
    private var serviceApi: ServiceApi? = null

    @Before
    override fun setUp() {
        super.setUp()
        initMocks(this)

        val rest = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        serviceApi = rest.create(ServiceApi::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun nowPlaying() {
        //
        // Arrange
        //
        val mapToSend = HashMap<String, Int>()
        mapToSend["page"] = 1

        //
        // Act
        //
        val testObserver = serviceApi!!.nowPlaying(API_TOKEN, mapToSend).test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val serviceResponse = testObserver.events[0][0] as ServiceResponse
        assertThat(serviceResponse.page).isEqualTo(1)
        assertThat(serviceResponse.results).isNotNull()
        assertThat(serviceResponse.dates).isNotNull()
        assertThat(serviceResponse.total_pages).isGreaterThan(0)
        assertThat(serviceResponse.total_results).isGreaterThan(0)
    }

    companion object {
        private val API_TOKEN = "6efc30f1fdcbe7425ab08503f07e2762"
    }
}
