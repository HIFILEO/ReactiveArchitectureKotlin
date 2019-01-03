package com.example.reactivearchitecture.model

import com.example.reactivearchitecture.categories.UnitTest
import com.example.reactivearchitecture.nowplaying.model.FilterManager
import com.example.reactivearchitecture.nowplaying.model.MovieInfo
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

import org.assertj.core.api.Assertions.assertThat
import java.util.ArrayList
import java.util.Date

import org.mockito.MockitoAnnotations.initMocks

@Category(UnitTest::class)
class FilterManagerTest {

    var movieInfoFullList: MutableList<MovieInfo> = ArrayList()

    var movieInfoHighRating: MovieInfo = MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            Date(),
            9.0)

    var movieInfoLowRating: MovieInfo = MovieInfoImpl(
            "www.url_low.com",
            "Dan IS STILL The Man",
            Date(),
            5.0)

    @Before
    fun setUp() {
        initMocks(this)

        for (i in 0..9) {
            if (i < 5) {
                movieInfoFullList.add(movieInfoHighRating)
            } else {
                movieInfoFullList.add(movieInfoLowRating)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterList_filterOn() {
        //
        // Arrange
        //
        val filterManager = FilterManager(true)

        //
        // Act
        //
        val listToTest = filterManager.filterList(movieInfoFullList)

        //
        // Assert
        //
        assertThat(filterManager.isFilterOn).isTrue()
        assertThat(listToTest).hasSize(5)
        assertThat(listToTest[0]).isEqualToComparingFieldByField(movieInfoHighRating)
    }

    @Test
    @Throws(Exception::class)
    fun filterList_filterOff() {
        //
        // Arrange
        //
        val filterManager = FilterManager(false)

        //
        // Act
        //
        val listToTest = filterManager.filterList(movieInfoFullList)

        //
        // Assert
        //
        assertThat(filterManager.isFilterOn).isFalse()
        assertThat(listToTest).hasSize(10)
        assertThat(listToTest[0]).isEqualToComparingFieldByField(movieInfoHighRating)
        assertThat(listToTest[9]).isEqualToComparingFieldByField(movieInfoLowRating)
    }

    @Test
    @Throws(Exception::class)
    fun getFullList_filterOff() {
        //
        // Arrange
        //
        val filterManager = FilterManager(false)

        //
        // Act
        //
        filterManager.filterList(movieInfoFullList)
        val listToTest = filterManager.getFullList()

        //
        // Assert
        //
        assertThat(filterManager.isFilterOn).isFalse()
        assertThat(listToTest).hasSize(10)
        assertThat(listToTest[0]).isEqualToComparingFieldByField(movieInfoHighRating)
        assertThat(listToTest[9]).isEqualToComparingFieldByField(movieInfoLowRating)
    }

    @Test
    @Throws(Exception::class)
    fun getFullList_filterOn() {
        //
        // Arrange
        //
        val filterManager = FilterManager(true)

        //
        // Act
        //
        filterManager.filterList(movieInfoFullList)
        val listToTest = filterManager.getFullList()

        //
        // Assert
        //
        assertThat(filterManager.isFilterOn).isTrue()
        assertThat(listToTest).hasSize(5)
        assertThat(listToTest[0]).isEqualToComparingFieldByField(movieInfoHighRating)
    }
}