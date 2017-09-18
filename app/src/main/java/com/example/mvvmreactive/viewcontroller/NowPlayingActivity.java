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

package com.example.mvvmreactive.viewcontroller;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.mvvmreactive.R;
import com.example.mvvmreactive.adapter.NowPlayingListAdapter;
import com.example.mvvmreactive.databinding.ActivityNowPlayingBinding;
import com.example.mvvmreactive.model.MovieViewInfo;


import com.example.mvvmreactive.view.DividerItemDecoration;
import com.example.mvvmreactive.viewmodel.NowPlayingViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;


/**
 * This is the only activity for the application.
 */
public class NowPlayingActivity extends BaseActivity implements NowPlayingListAdapter.OnLoadMoreListener {
    private static final String LAST_SCROLL_POSITION = "LAST_SCROLL_POSITION";
    private static final String LOADING_DATA = "LOADING_DATA";
    private NowPlayingListAdapter nowPlayingListAdapter;
    private NowPlayingViewModel nowPlayingViewModel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActivityNowPlayingBinding nowPlayingBinding;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        //Retrieve ViewModel
        nowPlayingViewModel = ViewModelProviders.of(this, viewModelFactory).get(NowPlayingViewModel.class);

        //Create Binding
        nowPlayingBinding = DataBindingUtil.setContentView(this, R.layout.activity_now_playing);
        nowPlayingBinding.setViewModel(nowPlayingViewModel);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        //toolbar.setTitle(getString(R.string.now_playing));
        setSupportActionBar(nowPlayingBinding.toolbar);

        //Create Adapter
        createAdapter(savedInstanceState);

        //restore adapter
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAST_SCROLL_POSITION, nowPlayingBinding.recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putBoolean(LOADING_DATA, nowPlayingListAdapter.isLoadingMoreShowing());
    }

    @Override
    public void onStop() {
        super.onStop();

        //un-subscribe
        compositeDisposable.clear();

        //un-bind
        nowPlayingBinding.unbind();
    }

    private void showError() {
        Toast.makeText(this, R.string.error_msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Add list to adapter.
     * @param movieViewInfoList - list to add.
     */
    public void addToAdapter(List<MovieViewInfo> movieViewInfoList) {
        if (!movieViewInfoList.isEmpty()) {
            nowPlayingListAdapter.addList(movieViewInfoList);
        } else {
            nowPlayingListAdapter.disableLoadMore();
        }
    }

    /**
     * Restore the state of the screen.
     * @param savedInstanceState -
     */
    public void restoreState(Bundle savedInstanceState) {
        Parcelable savedRecyclerLayoutState =
                savedInstanceState.getParcelable(LAST_SCROLL_POSITION);
        nowPlayingBinding.recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
    }

    /**
     * Create the adapter for {@link RecyclerView}.
     */
    private void createAdapter(Bundle savedInstanceState) {
        nowPlayingBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        nowPlayingBinding.recyclerView.addItemDecoration(new DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                getResources().getColor(android.R.color.black, null)));
        nowPlayingListAdapter = new NowPlayingListAdapter(
                //shallow-copy
                new ArrayList<>(nowPlayingViewModel.getMovieViewInfoList()),
                this,
                nowPlayingBinding.recyclerView,
                savedInstanceState != null && savedInstanceState.getBoolean(LOADING_DATA));
        nowPlayingBinding.recyclerView.setAdapter(nowPlayingListAdapter);
    }

    @Override
    public void onLoadMore() {
        subscribeToMovieData(nowPlayingViewModel.loadMoreInfo());
    }

    /**
     * Bind to all data in {@link NowPlayingViewModel}.
     */
    private void bind() {
        subscribeToMovieData(nowPlayingViewModel.getMovieViewInfo());
    }

    /**
     * Subscribe to {@link MovieViewInfo}.
     * @param movieDataObservable -
     */
    private void subscribeToMovieData(Observable<List<MovieViewInfo>> movieDataObservable) {
        compositeDisposable.add(movieDataObservable
                //observe down
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<MovieViewInfo>>() {
                    @Override
                    public void accept(@NonNull List<MovieViewInfo> movieViewInfos) throws Exception {
                        addToAdapter(movieViewInfos);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        showError();

                        //try again
                        onLoadMore();
                    }
                }));
    }
}
