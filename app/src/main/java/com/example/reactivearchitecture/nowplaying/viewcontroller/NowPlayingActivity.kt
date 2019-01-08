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

package com.example.reactivearchitecture.nowplaying.viewcontroller

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import android.widget.Spinner
import android.widget.Toast

import com.example.reactivearchitecture.R

import com.example.reactivearchitecture.core.view.DividerItemDecoration
import com.example.reactivearchitecture.core.viewcontroller.BaseActivity

import com.example.reactivearchitecture.databinding.ActivityNowPlayingBinding
import com.example.reactivearchitecture.nowplaying.adapter.ScrollEventCalculator
import com.example.reactivearchitecture.nowplaying.adapter.filter.FilterAdapter
import com.example.reactivearchitecture.nowplaying.adapter.nowplaying.NowPlayingListAdapter
import com.example.reactivearchitecture.nowplaying.model.AdapterCommand
import com.example.reactivearchitecture.nowplaying.model.event.FilterEvent
import com.example.reactivearchitecture.nowplaying.model.event.ScrollEvent
import com.example.reactivearchitecture.nowplaying.model.uimodel.UiModel
import com.example.reactivearchitecture.nowplaying.view.FilterView
import com.example.reactivearchitecture.nowplaying.view.InProgresMovieViewInfoImpl
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo
import com.example.reactivearchitecture.nowplaying.viewmodel.NowPlayingViewModel
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.widget.RxAdapterView

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * This is the only activity for the application.
 */
class NowPlayingActivity : BaseActivity() {
    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var nowPlayingViewModel: NowPlayingViewModel
    private lateinit var nowPlayingBinding: ActivityNowPlayingBinding

    private var nowPlayingListAdapter: NowPlayingListAdapter? = null
    private val compositeDisposable = CompositeDisposable()
    private var scrollDisposable: Disposable? = null
    private var savedRecyclerLayoutState: Parcelable? = null
    private var latestUiModel: UiModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        // Create / Retrieve ViewModel
        nowPlayingViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(NowPlayingViewModel::class.java)

        // Create & Set Binding
        nowPlayingBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_now_playing)
        nowPlayingBinding.viewModel = nowPlayingViewModel

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(nowPlayingBinding.toolbar)

        // restore
        var savedUiModel: UiModel? = null
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(LAST_SCROLL_POSITION)
            savedUiModel = savedInstanceState.getParcelable(LAST_UIMODEL)
        }

        // init viewModel
        nowPlayingViewModel.init(savedUiModel)
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
                LAST_SCROLL_POSITION,
                nowPlayingBinding.recyclerView.layoutManager?.onSaveInstanceState()
        )
        outState.putParcelable(LAST_UIMODEL, latestUiModel)
    }

    public override fun onStop() {
        super.onStop()

        // un-subscribe
        compositeDisposable.clear()
    }

    public override fun onDestroy() {
        super.onDestroy()

        // un-bind (Android Databinding)
        nowPlayingBinding.unbind()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //
        // Inflate
        //
        menuInflater.inflate(R.menu.menu_action_bar_spinner, menu)
        val filterSpinner = menu.findItem(R.id.filterSpinner).actionView as Spinner
        filterSpinner.adapter = createFilterAdapter()

        //
        // Restore
        //
        // Note - menu items get created AFTER onStart(). So must restore here.
        if (latestUiModel?.isFilterOn == true) {
            filterSpinner.setSelection(1)
        } else {
            filterSpinner.setSelection(0)
        }

        //
        // Bind to Spinner
        //
        compositeDisposable.add(RxAdapterView.itemSelections(filterSpinner)
                // When binding for the first time, the previous restore triggers a filter command
                // twice. Not needed!
                .skip(2)
                .map { integer -> FilterEvent(integer == 1) }
                .subscribe(
                    { filterEvent -> nowPlayingViewModel.processUiEvent(filterEvent) },
                    { throw UnsupportedOperationException(
                            "Errors in filter event unsupported. Crash app." + it.localizedMessage)
                    }
                )
        )

        return true
    }

    /**
     * Create the adapter for [RecyclerView].
     * @param adapterList - List that backs the adapter.
     */
    private fun createAdapter(adapterList: MutableList<MovieViewInfo>) {
        val linearLayoutManager = LinearLayoutManager(this)

        nowPlayingBinding.recyclerView.layoutManager = linearLayoutManager
        nowPlayingBinding.recyclerView.addItemDecoration(DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                resources.getColor(android.R.color.black, null)))
        nowPlayingListAdapter = NowPlayingListAdapter(adapterList)
        nowPlayingBinding.recyclerView.adapter = nowPlayingListAdapter
    }

    /**
     * Bind to all data in [NowPlayingViewModel].
     */
    private fun bind() {
        //
        // Bind to UiModel
        //
        compositeDisposable.add(
                requireNotNull(nowPlayingViewModel.uiModels) {"UiModel is null in bind()"}
                .subscribe(
                        { this.processUiModel(it) },
                        { throw UnsupportedOperationException(
                                "Errors from Model Unsupported: " + it.localizedMessage)
                        }
                )
        )
    }

    /**
     * Bind to scroll events.
     */
    private fun bindToScrollEvent() {
        //
        // Guard
        //
        if (scrollDisposable != null) {
            return
        }

        //
        // Bind
        //
        scrollDisposable = RxRecyclerView.scrollEvents(nowPlayingBinding.recyclerView)
                // Bind to RxBindings.
                .flatMap<ScrollEvent> { recyclerViewScrollEvent ->
                    val scrollEventCalculator = ScrollEventCalculator(recyclerViewScrollEvent)

                    // Only handle 'is at end' of list scroll events
                    if (scrollEventCalculator.isAtScrollEnd) {
                        val scrollEvent = ScrollEvent()
                        scrollEvent.pageNumber = (latestUiModel?.pageNumber ?: 0) + 1
                        Observable.just(scrollEvent)
                    } else {
                        Observable.empty()
                    }
                }
                // Filter any multiple events before 250MS
                .debounce { Observable.empty<Any>().delay(250, TimeUnit.MILLISECONDS) }
                // Send ScrollEvent to ViewModel
                .subscribe(
                    { scrollEvent -> nowPlayingViewModel.processUiEvent(scrollEvent) },
                    { throw UnsupportedOperationException(
                            "Errors in scroll event unsupported. Crash app." + it.localizedMessage)
                    }
                )
                .also {
                    compositeDisposable.add(it)
                }
    }

    /**
     * Unbind from scroll events.
     */
    private fun unbindFromScrollEvent() {
        scrollDisposable?.let {
            it.dispose()
            compositeDisposable.delete(it)
            scrollDisposable = null
        }
    }

    /**
     * Bind to [UiModel].
     * @param uiModel - the [UiModel] from [NowPlayingViewModel] that backs the UI.
     */
    private fun processUiModel(uiModel: UiModel) {
        /*
        Note - Keep the logic here as SIMPLE as possible.
         */
        Timber.i(
            "Thread name: %s. Update UI based on UiModel.",
            Thread.currentThread().name
        )
        this.latestUiModel = uiModel

        //
        // Update progressBar
        //
        nowPlayingBinding.progressBar.visibility =
                if (uiModel.isFirstTimeLoad) View.VISIBLE else View.GONE

        //
        // Scroll Listener
        //
        if (uiModel.isEnableScrollListener) {
            bindToScrollEvent()
        } else {
            unbindFromScrollEvent()
        }

        //
        // Update adapter
        //
        // Note - local declaration to remove !!
        val nowPlayingListAdapter = nowPlayingListAdapter

        if (nowPlayingListAdapter == null) {
            // create adapter data
            val adapterData: ArrayList<MovieViewInfo> = if (uiModel.currentList.isEmpty()) {
                ArrayList()
            } else {
                uiModel.currentList as ArrayList<MovieViewInfo>
            }

            when(uiModel.adapterCommandType) {
                AdapterCommand.ADD_DATA_ONLY, AdapterCommand.ADD_DATA_REMOVE_IN_PROGRESS,
                AdapterCommand.SWAP_LIST_DUE_TO_NEW_FILTER ->
                    adapterData.addAll(uiModel.resultList)
                AdapterCommand.SHOW_IN_PROGRESS ->
                    adapterData.add(InProgresMovieViewInfoImpl())
            }

            // create adapter
            createAdapter(adapterData)

            // Restore adapter state
            if (savedRecyclerLayoutState != null) {
                nowPlayingBinding.recyclerView.layoutManager
                        ?.onRestoreInstanceState(savedRecyclerLayoutState)
            }

        } else {

            when (uiModel.adapterCommandType) {
                AdapterCommand.ADD_DATA_REMOVE_IN_PROGRESS -> {
                    //Remove Null Spinner
                    if (nowPlayingListAdapter.itemCount > 0) {
                        val itemToRemove = nowPlayingListAdapter.getItem(
                                nowPlayingListAdapter.itemCount - 1)
                        nowPlayingListAdapter.remove(itemToRemove)
                    }

                    // Add Data
                    nowPlayingListAdapter.addList(uiModel.resultList)
                }
                AdapterCommand.ADD_DATA_ONLY ->
                    // Add Data
                    nowPlayingListAdapter.addList(uiModel.resultList)
                AdapterCommand.SHOW_IN_PROGRESS -> {
                    // Add null to adapter. Null shows spinner in Adapter logic.
                    nowPlayingListAdapter.add(InProgresMovieViewInfoImpl())
                    nowPlayingBinding.recyclerView.scrollToPosition(
                            nowPlayingListAdapter.itemCount - 1
                    )
                }
                AdapterCommand.SWAP_LIST_DUE_TO_NEW_FILTER -> {
                    // create adapter data
                    val adapterData: ArrayList<MovieViewInfo> = if (uiModel.currentList.isEmpty()) {
                        ArrayList()
                    } else {
                        uiModel.currentList as ArrayList<MovieViewInfo>
                    }

                    // Check if loading was in progress
                    val itemCount = nowPlayingListAdapter.itemCount
                    if (itemCount > 0 && nowPlayingListAdapter.getItem(itemCount - 1) is InProgresMovieViewInfoImpl) {
                        adapterData.add(InProgresMovieViewInfoImpl())
                    }

                    //swap
                    nowPlayingListAdapter.replace(adapterData)
                }
            }
        }

        //
        // Error Messages
        //
        if (uiModel.failureMsg != null && !uiModel.failureMsg.isEmpty()) {
            Toast.makeText(this@NowPlayingActivity, R.string.error_msg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Create the filter list adapter.
     * @return - adapter to show for filtering.
     */
    private fun createFilterAdapter(): FilterAdapter {
        val filterViewOn = FilterView(
                getString(R.string.filter_on),
                R.drawable.star_filled,
                true)

        val filterViewOff = FilterView(
                getString(R.string.filter_off),
                R.drawable.star_empty,
                false)

        val filterViewList = ArrayList<FilterView>()
        filterViewList.add(filterViewOff)
        filterViewList.add(filterViewOn)

        return FilterAdapter(this, filterViewList)
    }

    companion object {
        private const val LAST_SCROLL_POSITION = "LAST_SCROLL_POSITION"
        private const val LAST_UIMODEL = "LAST_UIMODEL"
    }
}
