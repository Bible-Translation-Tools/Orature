package org.wycliffeassociates.otter.common.io.zip

import io.reactivex.Observable
import java.io.BufferedReader
import java.io.File
import java.io.InputStream

interface IFileReader : AutoCloseable {
    fun bufferedReader(filepath: String): BufferedReader

    fun stream(filepath: String): InputStream

    fun exists(filepath: String): Boolean

    fun copyDirectory(
        source: String,
        destinationDirectory: File,
        filter: (String) -> Boolean = { _ -> true }
    ): Observable<String>

    fun list(directory: String): Sequence<String>
}
