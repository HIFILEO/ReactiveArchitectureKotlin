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

package com.example.reactivearchitecture.nowplaying.view

import android.annotation.SuppressLint

import com.example.reactivearchitecture.nowplaying.model.FilterManager
import com.example.reactivearchitecture.nowplaying.model.MovieInfo

import java.text.SimpleDateFormat

/**
 * View representation of movie information. The business logic for data manipulation that is
 * handled by the view data.
 */
class MovieViewInfoImpl(private val movieInfo: MovieInfo) : MovieViewInfo {
    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    override val pictureUrl: String
        get() = movieInfo.pictureUrl

    override val title: String
        get() = movieInfo.title

    override val releaseDate: String
        get() = dateFormat.format(movieInfo.releaseDate)

    override val rating: String
        get() = Math.round(movieInfo.rating).toString() + "/10"

    override val isHighRating: Boolean
        get() = Math.round(movieInfo.rating) >= FilterManager.RATE_NUMBER_TO_STAR
}
