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

import com.example.reactivearchitecture.nowplaying.model.action.FilterAction
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult

import io.reactivex.ObservableTransformer

/**
 * Holds the [io.reactivex.ObservableTransformer] for performing filtering with [FilterManager].
 *
 * @param filterManagerIn - [FilterManager] to use for transformers.
 */
class FilterTransformer(private val filterManager: FilterManager) {

    val transformFilterScrollResult: ObservableTransformer<ScrollResult, ScrollResult> =
        ObservableTransformer { upstream ->
            upstream.map { scrollResult: ScrollResult ->
                ScrollResult(
                        scrollResult.type,
                        scrollResult.isSuccessful,
                        scrollResult.isLoading,
                        scrollResult.pageNumber,
                        if (scrollResult.result == null)
                            null
                        else
                            filterManager.filterList(scrollResult.result),
                        scrollResult.error
                )
            }
        }

    val transformFilterRestoreResult: ObservableTransformer<RestoreResult, RestoreResult> =
        ObservableTransformer { upstream ->
            upstream.map { restoreResult: RestoreResult ->
                RestoreResult(
                        restoreResult.type,
                        restoreResult.isSuccessful,
                        restoreResult.isLoading,
                        restoreResult.pageNumber,
                        if (restoreResult.result == null)
                            null
                        else
                            filterManager.filterList(restoreResult.result),
                        restoreResult.error
                )
            }
        }

    val transformFilterActionToFilterResult: ObservableTransformer<FilterAction, FilterResult> =
        ObservableTransformer { upstream ->
            upstream.map { filterAcrtion: FilterAction ->
                filterManager.isFilterOn = filterAcrtion.isFilterOn

                FilterResult.success(
                        filterManager.isFilterOn,
                        filterManager.fullList
                )
            }
        }
}
