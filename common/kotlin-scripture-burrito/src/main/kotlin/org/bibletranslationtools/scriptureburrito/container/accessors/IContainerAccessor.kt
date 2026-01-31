package org.bibletranslationtools.scriptureburrito.container.accessors

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

interface IContainerAccessor: AutoCloseable {
    fun fileExists(filename: String): Boolean

    fun list(path: String): List<String>
    fun getInputStream(filename: String): InputStream
    fun getInputStreams(path: String, extension: String): Map<String, InputStream>
    
    fun getInputStreams(
        path: String,
        extensions: List<String> = listOf()
    ): Map<String, InputStream>
    fun getReader(filename: String): Reader
    fun initWrite()
    fun write(filename: String, writeFunction: (OutputStream) -> Unit)
    fun write(files: Map<String, (OutputStream) -> Unit>)
    
    val root: String?
}