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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.example.reactivearchitecture.R
import com.example.reactivearchitecture.nowplaying.view.FilterView

/**
 * Adapter that shows a list of [FilterView].
 */
class FilterAdapter(context: Context, filterViewList: List<FilterView>)
    : ArrayAdapter<FilterView>(context, 0, filterViewList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewToFill: View

        // Note - Dan L - in Kotlin, if returns an expression so this is perfectly legal op
        val filterViewHolder: FilterViewHolder = if (convertView == null) {
            viewToFill = LayoutInflater.from(context).inflate(R.layout.filter_item, null)

            // Note - Dan L - in Kotlin, the last entry in the block is returned as part of the
            // expression. In other words, this is the assignment.
            FilterViewHolder(viewToFill)
        } else {
            viewToFill = convertView
            viewToFill.tag as FilterViewHolder
        }

        filterViewHolder.bind(getItem(position))
        viewToFill.tag = filterViewHolder

        return viewToFill
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewToFill = convertView
        val filterViewHolder: FilterViewHolder

        if (viewToFill == null) {
            viewToFill = LayoutInflater.from(context)
                    .inflate(R.layout.filter_dropdown_item, null)

            filterViewHolder = FilterViewHolder(viewToFill)
        } else {
            filterViewHolder = viewToFill.tag as FilterViewHolder
        }

        filterViewHolder.bind(getItem(position))
        viewToFill!!.tag = filterViewHolder

        return viewToFill
    }
}
