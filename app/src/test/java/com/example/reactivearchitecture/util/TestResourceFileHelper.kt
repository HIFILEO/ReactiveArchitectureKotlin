package com.example.reactivearchitecture.util

import android.content.Context

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Helper for reading json data files from src/test/resources.
 *
 * Note - object - singleton utility FTW
 */
object TestResourceFileHelper {
    private const val RESOURCE_PATH = "/src/test/resources/"

    /**
     * Return the contents of the file with the provided fileName from the test resource directory.
     *
     * @param context - The Context to use when loading the resource.
     * @param fileName - The name of the file in the test resource directory.
     * @return The contents of the file with the provided fileName from the test resource directory.
     * @throws Exception if something goes wrong.
     */
    @Throws(Exception::class)
    fun getFileContentsAsString(context: Context, fileName: String): String {
        val filePath = context.packageResourcePath + RESOURCE_PATH + fileName
        val jsonFile = File(filePath)
        val inputStream = FileInputStream(jsonFile)
        val contents = StringBuilder()
        val br = BufferedReader(InputStreamReader(inputStream))
        var line: String? = br.readLine()
        while (line != null) {
            contents.append(line)
            line = br.readLine()
        }
        return contents.toString()
    }

    /**
     * Return the contents of the file with the provided fileName from the test resource directory.
     *
     * @param callerClass - test class making the request.
     * @param fileName - The name of the file in the test resource directory.
     * @return The contents of the file with the provided fileName from the test resource directory.
     * @throws Exception if something goes wrong.
     */
    @Throws(Exception::class)
    fun getFileContentAsString(callerClass: Any, fileName: String): String {
        val classLoader = callerClass.javaClass.classLoader
        val jsonFile = File(classLoader!!.getResource(fileName).file)
        val inputStream = FileInputStream(jsonFile)
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
