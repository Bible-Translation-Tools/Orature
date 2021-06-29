/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.io.zip

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.streams.asSequence

class NioZipFileReader(
    zipFile: File
) : IFileReader {
    private val fileSystem: FileSystem = FileSystems.newFileSystem(zipFile.jarUri(), mapOf("create" to "false"))

    override fun close() = fileSystem.close()

    override fun exists(filepath: String) = Files.exists(fileSystem.getPath(filepath))

    override fun list(directory: String): Sequence<String> {
        return Files
            .list(fileSystem.getPath(directory))
            .map { it.toString() }
            .asSequence()
    }

    override fun bufferedReader(filepath: String) = Files.newBufferedReader(fileSystem.getPath(filepath))!!

    override fun stream(filepath: String): InputStream = Files.newInputStream(fileSystem.getPath(filepath))

    override fun copyDirectory(
        source: String,
        destinationDirectory: File,
        filter: (String) -> Boolean
    ): Observable<String> {
        val sourcePath = fileSystem.getPath(source)
        val destPath = destinationDirectory.toPath()
        return sourcePath.copyDirectoryTo(destPath, filter)
    }
}
