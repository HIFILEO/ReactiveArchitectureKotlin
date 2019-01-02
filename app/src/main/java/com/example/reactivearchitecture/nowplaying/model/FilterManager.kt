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

package com.example.reactivearchitecture.nowplaying.model

import java.util.ArrayList

/**
 * Manages all data. Filters data based on those that have an eight or above rating.
 * @param filterOn - true to turn filter on, false otherwise.
 */
class FilterManager(
    @set:Synchronized
    var isFilterOn: Boolean
) {
    private val filteredList = ArrayList<MovieInfo>()
    private val fullList = ArrayList<MovieInfo>()

    /**
     * Filter a list of data. Retrains all entries passed in.
     * @param listToFilter -  list to retain as well as filter.
     * @return a new filtered list or original un-filtered list that was  passed in.
     */
    @Synchronized
    fun filterList(listToFilter: List<MovieInfo>): List<MovieInfo> {
        fullList.addAll(listToFilter)

        val filteredData = filterData(listToFilter)
        return if (isFilterOn) {
            filteredData
        } else {
            listToFilter
        }
    }

    /**
     * Get full list based on [FilterManager.filterOn].
     * @return - full list of filtered or unfiltered data.
     */
    @Synchronized
    fun getFullList(): List<MovieInfo> {
        return if (isFilterOn) {
            ArrayList(filteredList)
        } else {
            ArrayList(fullList)
        }
    }

    /**
     * Filter data, and store for later use. Return filtered list.
     * @param listToFilter - list to filter.
     * @return filtered list.
     */
    private fun filterData(listToFilter: List<MovieInfo>): List<MovieInfo> {
        val filteredList = ArrayList<MovieInfo>()

        for (movieInfo in listToFilter) {
            if (Math.round(movieInfo.rating) >= RATE_NUMBER_TO_STAR) {
                filteredList.add(movieInfo)
            }
        }

        this.filteredList.addAll(filteredList)

        return filteredList
    }

    companion object {
        val RATE_NUMBER_TO_STAR = 8
    }
}
