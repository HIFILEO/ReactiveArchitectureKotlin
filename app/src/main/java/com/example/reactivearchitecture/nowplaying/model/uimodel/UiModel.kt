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

package com.example.reactivearchitecture.nowplaying.model.uimodel

import android.os.Parcel
import android.os.Parcelable

import com.example.reactivearchitecture.nowplaying.model.AdapterCommand
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo

/**
 * The model the UI will bind to.
 * Note - fields in this class should be immutable for "Scan" safety.
 */
class UiModel : Parcelable {
    val isFirstTimeLoad: Boolean
    val failureMsg: String
    val pageNumber: Int
    var isEnableScrollListener: Boolean
    var currentList: List<MovieViewInfo>
    var resultList: List<MovieViewInfo>
    @AdapterCommand.AdapterCommandType var adapterCommandType: Int
    var isFilterOn: Boolean

    /**
     * Construct class using android [Parcelable]
     * @param in - [Parcelable]
     */
    protected constructor(`in`: Parcel) {
        this.isFirstTimeLoad = false
        this.failureMsg = ""
        this.pageNumber = `in`.readInt()
        this.isEnableScrollListener = false
        this.currentList = emptyList()
        this.resultList = emptyList()
        this.adapterCommandType = AdapterCommand.DO_NOTHING
        this.isFilterOn = `in`.readByte().toInt() != 0
    }

    /**
     * Construct class
     * @param firstTimeLoad - true if first time loading, false otherwise
     * @param failureMsg - failure message to show user, empty otherwise
     * @param pageNumber - current loaded page number, 0 otherwise
     * @param enableScrollListener - scroll listener is enabled, false otherwise
     * @param currentList - current data to show in adapter, empty otherwise
     * @param resultList - result of loading new data, empty otherwise
     * @param adapterCommandType - the latest command issued by the adapter. DO_NOTHING otherwise.
     * @param filterOn - is the filter on? false otherwise
     */
    constructor(
        firstTimeLoad: Boolean = true,
        failureMsg: String = "",
        pageNumber: Int = 0,
        enableScrollListener: Boolean = false,
        currentList: List<MovieViewInfo> = emptyList(),
        resultList: List<MovieViewInfo> = emptyList(),
        adapterCommandType: Int = AdapterCommand.DO_NOTHING,
        filterOn: Boolean = false
    ) {
        this.isFirstTimeLoad = firstTimeLoad
        this.failureMsg = failureMsg
        this.pageNumber = pageNumber
        this.isEnableScrollListener = enableScrollListener
        this.currentList = currentList
        this.resultList = resultList
        this.adapterCommandType = adapterCommandType
        this.isFilterOn = filterOn
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // only care about pageNumber when saving state
        parcel.writeInt(pageNumber)
        parcel.writeByte((if (isFilterOn) 1 else 0).toByte())
    }

    companion object {

        // TODO - update to the kotlin way for parcelable
        @JvmField
        val CREATOR: Parcelable.Creator<UiModel> = object : Parcelable.Creator<UiModel> {
            override fun createFromParcel(`in`: Parcel): UiModel {
                return UiModel(`in`)
            }

            override fun newArray(size: Int): Array<UiModel?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * Create createNonInjectedData state.
         * Note - this can't be a static final because you write espresso tests and you'll end up duplicating data.
         * @return - new UiModel in createNonInjectedData state.
         */
        fun initState(): UiModel {
            return UiModel(
                true,
                    "",
                    0,
                    false,
                    emptyList(),
                    emptyList(),
                    AdapterCommand.DO_NOTHING,
                    false
            )
        }
    }
}
