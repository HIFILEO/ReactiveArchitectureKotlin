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

package com.example.reactivearchitecture.nowplaying.adapter.filter

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.example.reactivearchitecture.R
import com.example.reactivearchitecture.core.adapter.BaseViewHolder
import com.example.reactivearchitecture.nowplaying.view.FilterView

/**
 * [FilterView] holder.
 */
class FilterViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val filterImageView: ImageView = itemView.findViewById(R.id.filterImageView)
    private val filterTextView: TextView = itemView.findViewById(R.id.filterTextView)

    /**
     * Bind the [FilterView] to the view holder.
     * @param filterView - data to bind with.
     */
    fun bind(filterView: FilterView) {
        filterImageView.setImageResource(filterView.drawableResource)
        filterTextView.text = filterView.filterText
    }
}
