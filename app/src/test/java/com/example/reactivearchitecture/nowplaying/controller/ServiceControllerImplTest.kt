package com.example.reactivearchitecture.nowplaying.controller

import com.example.reactivearchitecture.categories.UnitTest
import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo
import com.example.reactivearchitecture.rx.RxJavaTest
import com.example.reactivearchitecture.nowplaying.service.ServiceResponse
import com.example.reactivearchitecture.util.TestResourceFileHelper
import com.google.gson.Gson

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

import io.reactivex.observers.TestObserver

import com.ibm.icu.impl.Assert.fail
import org.assertj.core.api.Assertions.assertThat

@Category(UnitTest::class)
class ServiceControllerImplTest : RxJavaTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    @Throws(Exception::class)
    fun testTranslateNowPlaying() {
        //
        // Arrange
        //
        val testObserver: TestObserver<NowPlayingInfo>
        var json: String? = null
        try {
            json = TestResourceFileHelper.getFileContentAsString(this, "now_playing_page_1.json")
        } catch (e: Exception) {
            fail(e)
        }

        val serviceResponse = Gson().fromJson(json, ServiceResponse::class.java)

        val translateNowPlayingSubscriptionFunc = ServiceControllerImpl
                .TranslateNowPlayingSubscriptionFunc(IMAGE_PATH)

        //
        // Act
        //
        testObserver = translateNowPlayingSubscriptionFunc.apply(serviceResponse).test()
        testScheduler.triggerActions()

        //
        // Assert
        //
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val nowPlayingInfo = testObserver.events[0][0] as NowPlayingInfo
        assertThat(nowPlayingInfo).isNotNull()
        assertThat(nowPlayingInfo.pageNumber).isEqualTo(1)
        assertThat(nowPlayingInfo.totalPageNumber).isEqualTo(35)
        assertThat(nowPlayingInfo.movies.size).isEqualTo(20)
        assertThat(nowPlayingInfo.movies).isNotNull

        val movieInfo: MovieInfo = nowPlayingInfo.movies[0]
        assertThat(movieInfo.pictureUrl)
                .isEqualToIgnoringCase("$IMAGE_PATH/tnmL0g604PDRJwGJ5fsUSYKFo9.jpg")
        assertThat(movieInfo.rating).isEqualTo(7.2)
        assertThat(movieInfo.releaseDate.toString())
                .isEqualToIgnoringCase("Wed Mar 15 00:00:00 EDT 2017")
        assertThat(movieInfo.title).isEqualToIgnoringCase("Beauty and the Beast")
    }

    companion object {
        private const val IMAGE_PATH = "www.imagepath.com"
    }
}