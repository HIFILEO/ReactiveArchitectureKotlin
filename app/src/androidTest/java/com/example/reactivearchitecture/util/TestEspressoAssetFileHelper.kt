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

package com.example.reactivearchitecture.util

import android.content.Context

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Helper for reading json data files from assets folder for Espresso Tests.
 *
 * Note - object - singleton utility FTW
 */
object TestEspressoAssetFileHelper {

    /**
     * Return the contents of the file with the provided fileName from the test resource directory.
     *
     * @param context - Context of this instrumentation's package.
     * @param fileName - The name of the file in the test resource directory.
     * @return The contents of the file with the provided fileName from the test resource directory.
     * @throws Exception if something goes wrong.
     */
    @Throws(Exception::class)
    fun getFileContentAsString(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val contents = StringBuilder()
        val br = BufferedReader(InputStreamReader(inputStream))
        var line: String? = br.readLine()
        while (line != null) {
            contents.append(line)
            line = br.readLine()
        }
        return contents.toString()
    }
}
