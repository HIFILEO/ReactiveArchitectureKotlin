// CHECKSTYLE:OFF
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

/**
 * Service response based on:
 * https://developers.themoviedb.org/3/movies/get-now-playing
 */
class Results {
    val poster_path: String? = null
    val isAdult: Boolean = false
    val overview: String? = null
    val release_date: String? = null
    val genre_ids: IntArray? = null
    val id: Int = 0
    val original_title: String? = null
    val original_language: String? = null
    val title: String? = null
    val backdrop_path: String? = null
    val popularity: Double = 0.toDouble()
    val vote_count: Int = 0
    val isVideo: Boolean = false
    val vote_average: Double = 0.toDouble()
}
