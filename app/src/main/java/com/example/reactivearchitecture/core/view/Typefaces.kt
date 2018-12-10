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
import android.graphics.Typeface
import android.util.Log

import java.util.Hashtable

/**
 * Typeface has issues, must use this class for loading fonts.
 */
internal object Typefaces {
    private val TAG = "Typefaces"
    private val CACHE = Hashtable<String, Typeface>()

    /**
     * Get one typeface per application.
     * @param context - context
     * @param assetPath - path to asset folder
     * @return - The [Typeface] to use
     */
    operator fun get(context: Context, assetPath: String): Typeface? {
        synchronized(CACHE) {
            if (!CACHE.containsKey(assetPath)) {
                try {
                    val typeface = Typeface.createFromAsset(context.resources.assets,
                            assetPath)
                    CACHE[assetPath] = typeface
                } catch (e: Exception) {
                    Log.e(TAG, "Could not get typeface '" + assetPath +
                            "' because " + e.message)
                    return null
                }
            }
            return CACHE[assetPath]
        }
    }
}
