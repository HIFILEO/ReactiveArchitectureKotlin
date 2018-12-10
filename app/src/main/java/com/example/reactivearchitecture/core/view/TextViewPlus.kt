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

package com.example.reactivearchitecture.core.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log

import com.example.reactivearchitecture.R

/**
 * Custom Text view to support fonts.
 */
class TextViewPlus : android.support.v7.widget.AppCompatTextView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setCustomFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle) {
        setCustomFont(context, attrs)
    }

    /**
     * Set the custom font.
     * @param ctx - context
     * @param asset - asset to load.
     * @return true if set, false otherwise.
     */
    fun setCustomFont(ctx: Context, asset: String?): Boolean {

        var tf: Typeface? = null
        try {
            tf = Typefaces.get(ctx, asset!!)
        } catch (e: Exception) {
            Log.e(TAG, "Could not get typeface: " + e.message)
        }

        if (tf != null) {
            typeface = tf
            return true
        }

        return false
    }

    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val typedArray: TypedArray = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewPlus)
        val customFont = typedArray.getString(R.styleable.TextViewPlus_customFont)
        setCustomFont(ctx, customFont)
        typedArray.recycle()
    }

    companion object {
        private val TAG = "TextView"
    }
}
