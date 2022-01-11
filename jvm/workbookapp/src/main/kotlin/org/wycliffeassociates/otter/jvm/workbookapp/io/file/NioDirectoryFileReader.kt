/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.io.file

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import org.wycliffeassociates.otter.jvm.workbookapp.io.zip.copyDirectoryTo
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class NioDirectoryFileReader(
    private val dir: File
) : IFileReader {
    private val fileSystem: FileSystem = FileSystems.getDefault()

    override fun bufferedReader(filepath: String) = Files.newBufferedReader(getAbsolutePath(filepath))!!

    override fun stream(filepath: String): InputStream = Files.newInputStream(getAbsolutePath(filepath))

    override fun exists(filepath: String) = Files.exists(getAbsolutePath(filepath))

    override fun copyDirectory(
        source: String,
        destinationDirectory: File,
        filter: (String) -> Boolean
    ): Observable<String> {
        val sourcePath = getAbsolutePath(source)
        val destPath = destinationDirectory.toPath()
        return sourcePath.copyDirectoryTo(destPath, filter)
    }

    override fun list(directory: String): Sequence<String> {
        return Files
            .list(getAbsolutePath(directory))
            .map { dir.toPath().relativize(it).toString() }
            .asSequence()
    }

    override fun close() {
        // No need to close this file system
        // Default file system cannot be closed
    }

    private fun getAbsolutePath(path: String): Path {
        return fileSystem.getPath(dir.absolutePath, path)
    }
}
