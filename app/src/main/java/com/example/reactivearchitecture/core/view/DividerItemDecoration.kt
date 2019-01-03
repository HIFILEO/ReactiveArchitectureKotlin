/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reactivearchitecture.core.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * I don't know why Google does this sometimes,
 * but here's a class to put dividers in recycler view.
 */
class DividerItemDecoration : RecyclerView.ItemDecoration {
    private var dividerDrawable: Drawable? = null
    private var orientation: Int = 0
    private var color: Int = 0

    private var additionalLeftPadding: Int = 0
    private var additionalTopPadding: Int = 0
    private var additionalRightPadding: Int = 0
    private var additionalBottomPadding: Int = 0

    /**
     * constructor needs a context and an orientation.
     * Looks like it defines the values you can use
     * @param context a context
     * @param orientation orientation as defined in the contants
     * @param color the color of the divider
     */
    constructor(context: Context, orientation: Int, color: Int) {
        val a: TypedArray = context.obtainStyledAttributes(ATTRS)
        dividerDrawable = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
        this.color = color
    }

    /**
     * Constructor for an RecyclerView.ItemDecoration that can be used for item spacing.
     * @param orientation VERTICAL_LIST or HORIZONTAL_LIST
     * @param color color to set the dividerDrawable
     * @param dividerDrawable the Drawable to use as the divider.
     */
    constructor(orientation: Int, color: Int, dividerDrawable: Drawable) {
        this.dividerDrawable = dividerDrawable
        this.color = color
        setOrientation(orientation)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, dividerDrawable!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, dividerDrawable!!.intrinsicWidth, 0)
        }
    }

    /**
     * Set orientation on this view.
     * @param orientation HORZ or VERT as defined above
     */
    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }
        this.orientation = orientation
    }

    /**
     * Draws a vertical divider.
     * @param c a canvas
     * @param parent the recycler view
     */
    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft + additionalLeftPadding
        val right = parent.width - parent.paddingRight - additionalRightPadding
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin + additionalTopPadding
            val bottom = top + dividerDrawable!!.intrinsicHeight - additionalBottomPadding
            dividerDrawable!!.setBounds(left, top, right, bottom)
            dividerDrawable!!.setColorFilter(color, PorterDuff.Mode.SRC)
            dividerDrawable!!.draw(c)
        }
    }

    /**
     * Draw a horizontal divider.
     * @param c a canvas
     * @param parent the recycler view
     */
    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop + additionalTopPadding
        val bottom = parent.height - parent.paddingBottom - additionalBottomPadding
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin + additionalLeftPadding
            val right = left + dividerDrawable!!.intrinsicWidth - additionalRightPadding
            dividerDrawable!!.setBounds(left, top, right, bottom)
            dividerDrawable!!.setColorFilter(color, PorterDuff.Mode.SRC)
            dividerDrawable!!.draw(c)
        }
    }

    /**
     * Add optional, additional padding to the decorator if you want it to inset from the
     * Recycle View itself.
     * @param left -
     * @param top -
     * @param right -
     * @param bottom -
     */
    fun setAdditionalPadding(left: Int, top: Int, right: Int, bottom: Int) {
        additionalLeftPadding = left
        additionalTopPadding = top
        additionalRightPadding = right
        additionalBottomPadding = bottom
    }

    companion object {

        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}
