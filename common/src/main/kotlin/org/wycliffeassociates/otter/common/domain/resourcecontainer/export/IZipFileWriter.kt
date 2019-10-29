package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import java.io.BufferedWriter
import java.io.File

interface IZipFileWriter : AutoCloseable {
    fun bufferedWriter(filepath: String): BufferedWriter
    fun copyDirectory(source: File, destination: String, filter: (String) -> Boolean = { _ -> true })
}