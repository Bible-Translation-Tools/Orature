package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import java.io.BufferedReader
import java.io.File

interface IZipFileReader : AutoCloseable {
    fun bufferedReader(filepath: String): BufferedReader
    fun exists(filepath: String): Boolean
    fun copyDirectory(source: String, destinationDirectory: File, filter: (String) -> Boolean = { _ -> true })
}