package com.example.reactivearchitecture.nowplaying.model.result

import com.example.reactivearchitecture.nowplaying.model.MovieInfo

/**
 * Results from [com.example.reactivearchitecture.nowplaying.model.action.FilterAction] requests.
 */
class FilterResult private constructor(
    @field:ResultType override val type: Int,
    val isFilterOn: Boolean,
    private val filterInProgress: Boolean,
    val filteredList: List<MovieInfo>?
) : Result() {

    /**
     * Note - These are CFM (Companion Facotry Methods) used instead of Java SFM
     * https://blog.kotlin-academy.com/effective-java-in-kotlin-item-1-consider-static-factory-methods-instead-of-constructors-8d0d7b5814b2
     */
    companion object {

        /**
         * In progress creator.
         * @param filterOn -
         * @return new [FilterResult]
         */
        fun inProgress(filterOn: Boolean): FilterResult {
            return FilterResult(
                    Result.IN_FLIGHT,
                    filterOn,
                    true,
                    null)
        }

        /**
         * Success Creator.
         * @param filterOn -
         * @param listToShow -
         * @return new [FilterResult]
         */
        fun success(filterOn: Boolean, listToShow: List<MovieInfo>): FilterResult {
            return FilterResult(
                    Result.SUCCESS,
                    filterOn,
                    true,
                    listToShow)
        }
    }
}
