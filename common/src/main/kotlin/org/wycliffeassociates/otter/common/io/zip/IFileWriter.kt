package org.wycliffeassociates.otter.common.io.zip

import java.io.BufferedWriter
import java.io.File

interface IFileWriter : AutoCloseable {
    fun bufferedWriter(filepath: String): BufferedWriter
    fun copyDirectory(source: File, destination: String, filter: (String) -> Boolean = { _ -> true })
    fun copyFile(source: File, destination: String)
}
