package com.example.reactivearchitecture.model

import com.example.reactivearchitecture.categories.UnitTest
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfoImpl

import org.junit.Test
import org.junit.experimental.categories.Category

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import org.assertj.core.api.Assertions.assertThat

@Category(UnitTest::class)
class MovieViewInfoImplTest {
    private val movieInfo = MovieInfoImpl(
            "www.pictureurl.com",
            "title",
            Date(),
            8.2
    )

    @Test
    @Throws(Exception::class)
    fun testMovieViewInfo() {
        //
        // Arrange
        //
        val movieViewInfo: MovieViewInfo = MovieViewInfoImpl(movieInfo)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        //
        // Act
        //

        //
        // Assert
        //
        assertThat(movieViewInfo.title).isEqualToIgnoringCase(movieInfo.title)
        assertThat(movieViewInfo.pictureUrl).isEqualToIgnoringCase(movieInfo.pictureUrl)
        assertThat(movieViewInfo.releaseDate)
                .isEqualToIgnoringCase(dateFormat
                        .format(movieInfo.releaseDate))
        assertThat(movieViewInfo.rating).isEqualToIgnoringCase("8/10")
        assertThat(movieViewInfo.isHighRating).isTrue()
    }
}