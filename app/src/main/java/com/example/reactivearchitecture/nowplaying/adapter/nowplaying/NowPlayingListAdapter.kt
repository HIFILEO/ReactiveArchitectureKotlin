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

package com.example.reactivearchitecture.nowplaying.adapter.nowplaying

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup

import com.example.reactivearchitecture.R
import com.example.reactivearchitecture.core.adapter.BaseViewHolder
import com.example.reactivearchitecture.core.adapter.RecyclerArrayAdapter
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo

/**
 * Adapter that shows a list of [MovieViewInfo] with continuous scrolling.
 *
 * @param objects - list of [MovieViewInfo]
 */
class NowPlayingListAdapter(objects: MutableList<MovieViewInfo?>)
    : RecyclerArrayAdapter<MovieViewInfo, BaseViewHolder>(objects) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_ITEM) {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                layoutInflater,
                R.layout.movie_item,
                parent,
                false
            )
            MovieViewHolder(binding)
        } else {
            val view = layoutInflater.inflate(R.layout.progress_item, parent, false)
            ProgressViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            (holder as MovieViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (super.getItem(position) != null) VIEW_ITEM else VIEW_PROGRESS
    }

    /**
     * Add a list of [MovieViewInfo] to the current adapter.
     *
     * @param movieViewInfoList - list to add
     */
    fun addList(movieViewInfoList: List<MovieViewInfo>) {
        for (movieViewInfo in movieViewInfoList) {
            add(movieViewInfo)
        }
    }

    companion object {
        private val VIEW_PROGRESS = 0
        private val VIEW_ITEM = 1
    }
}
