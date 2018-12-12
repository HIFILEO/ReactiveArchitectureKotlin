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

package com.example.reactivearchitecture.core.adapter

import android.support.v7.widget.RecyclerView

import java.util.Collections
import java.util.Comparator

/**
 * Array adapter for Recycler view. The ArrayAdapter for list view cannot be used.
 * Inspiration from:
 * https://gist.github.com/passsy/f8eecc97c37e3de46176
 *
 * @param <T> - Data Type
 * @param <V> - View Holder
 * @property objectList - mutable list of Data Type <T>
 * @constructor Creates an adapter backed by the data <T>
 */
abstract class RecyclerArrayAdapter<T, V : RecyclerView.ViewHolder>
(private var objectList: MutableList<T>?) : RecyclerView.Adapter<V>() {

    /**
     * Adds the specified t at the end of the array.
     *
     * @param `t` The <T> to add at the end of the array.
     */
    fun add(t: T) {
        objectList!!.add(t)
        notifyItemInserted(itemCount - 1)
    }

    /**
     * Remove all elements from the list.
     */
    fun clear() {
        val size = itemCount
        objectList!!.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemCount(): Int {
        return objectList!!.size
    }

    fun getItem(position: Int): T {
        return objectList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    fun getPosition(item: T): Int {
        return objectList!!.indexOf(item)
    }

    /**
     * Inserts the specified t at the specified index in the array.
     *
     * @param `t` The <T> to insert into the array.
     * @param index The index at which the t must be inserted.
     */
    fun insert(t: T, index: Int) {
        objectList!!.add(index, t)
        notifyItemInserted(index)
    }

    /**
     * Removes the specified t from the array.
     *
     * @param `t` The t to remove.
     */
    fun remove(t: T) {
        val position = getPosition(t)
        objectList!!.remove(t)
        notifyItemRemoved(position)
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objectList contained in this adapter.
     */
    fun sort(comparator: Comparator<in T>) {
        Collections.sort(objectList, comparator)
        notifyItemRangeChanged(0, itemCount)
    }

    /**
     * Replace the current list backing this adapter.
     *
     * @param newList - new list
     */
    fun replace(newList: MutableList<T>) {
        objectList = newList
        notifyDataSetChanged()
    }
}
