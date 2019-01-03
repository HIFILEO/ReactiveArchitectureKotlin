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

import java.util.ArrayList

/**
 * The model the UI will bind to.
 * Note - fields in this class should be immutable for "Scan" safety.
 */
class UiModel : Parcelable {
    var isFirstTimeLoad: Boolean = false
        private set
    var failureMsg: String? = null
        private set
    var pageNumber: Int = 0
        private set
    var isEnableScrollListener: Boolean = false
        private set
    var currentList: List<MovieViewInfo?>? = null
        private set
        get() {
            return ArrayList(field)
        }
    var resultList: List<MovieViewInfo>? = null
        private set
    @AdapterCommand.AdapterCommandType
    var adapterCommandType: Int = 0
        private set
    var isFilterOn: Boolean = false
        private set

    protected constructor(`in`: Parcel) {
        this.isFirstTimeLoad = false
        this.failureMsg = null
        this.pageNumber = `in`.readInt()
        this.isEnableScrollListener = false
        this.currentList = null
        this.resultList = null
        this.adapterCommandType = AdapterCommand.DO_NOTHING
        this.isFilterOn = `in`.readByte().toInt() != 0
    }

    private constructor(
        firstTimeLoad: Boolean,
        failureMsg: String?,
        pageNumber: Int,
        enableScrollListener: Boolean,
        currentList: List<MovieViewInfo?>?,
        resultList: List<MovieViewInfo>?,
        adapterCommandType: Int,
        filterOn: Boolean
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

//    /**
//     * Return a shallow copy of the current list.
//     * @return Shallow copy of list.
//     */
//    fun getCurrentList(): List<MovieViewInfo> {
//        return ArrayList(currentList!!)
//    }

    /**
     * Too many state? Too many params in constructors? Call on the builder pattern to Save The Day!.
     * TODO - get rid of builder pattern and use named parameters
     */
    class UiModelBuilder {
        private val uiModel: UiModel?

        private var firstTimeLoad: Boolean = false
        private var failureMsg: String? = null
        private var pageNumber: Int = 0
        private var enableScrollListener: Boolean = false
        private var currentList: List<MovieViewInfo?>? = null
        private var resultList: List<MovieViewInfo>? = null
        @AdapterCommand.AdapterCommandType
        private var adapterCommandType: Int = 0
        private var filterOn: Boolean = false

        /**
         * Construct Builder using defaults from previous [UiModel].
         * @param uiModel - model for builder to use.
         */
        constructor(uiModel: UiModel) {
            this.uiModel = uiModel

            this.firstTimeLoad = uiModel.isFirstTimeLoad
            this.failureMsg = uiModel.failureMsg
            this.pageNumber = uiModel.pageNumber
            this.enableScrollListener = uiModel.isEnableScrollListener
            this.currentList = uiModel.currentList
            this.resultList = uiModel.resultList
            this.adapterCommandType = uiModel.adapterCommandType
            this.filterOn = uiModel.isFilterOn
        }

        constructor() {
            this.uiModel = null
        }

        /**
         * Create the [UiModel] using the types in [UiModelBuilder].
         * @return new [UiModel].
         */
        fun createUiModel(): UiModel {
            if (currentList == null) {
                if (uiModel == null) {
                    currentList = ArrayList()
                } else {
                    // shallow copy
                    currentList = uiModel.currentList
                }
            }

            return UiModel(
                    firstTimeLoad,
                    failureMsg,
                    pageNumber,
                    enableScrollListener,
                    currentList,
                    resultList,
                    adapterCommandType,
                    filterOn)
        }

        fun setFirstTimeLoad(firstTimeLoad: Boolean): UiModelBuilder {
            this.firstTimeLoad = firstTimeLoad
            return this
        }

        fun setFailureMsg(failureMsg: String?): UiModelBuilder {
            this.failureMsg = failureMsg
            return this
        }

        fun setPageNumber(pageNumber: Int): UiModelBuilder {
            this.pageNumber = pageNumber
            return this
        }

        fun setEnableScrollListener(enableScrollListener: Boolean): UiModelBuilder {
            this.enableScrollListener = enableScrollListener
            return this
        }

        fun setCurrentList(currentList: List<MovieViewInfo>): UiModelBuilder {
            this.currentList = currentList
            return this
        }

        fun setResultList(resultList: List<MovieViewInfo>?): UiModelBuilder {
            this.resultList = resultList
            return this
        }

        fun setAdapterCommandType(adapterCommandType: Int): UiModelBuilder {
            this.adapterCommandType = adapterCommandType
            return this
        }

        fun setFilterOn(filterOn: Boolean): UiModelBuilder {
            this.filterOn = filterOn
            return this
        }
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
                    null,
                    0,
                    false,
                    ArrayList(),
                    null,
                    AdapterCommand.DO_NOTHING,
                    false
            )
        }
    }
}
