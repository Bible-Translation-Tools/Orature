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
package org.wycliffeassociates.otter.jvm.workbookapp.io.zip

import org.wycliffeassociates.otter.common.io.zip.IFileWriter
import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

class NioZipFileWriter(
    zipFile: File
) : IFileWriter {

    // useTempFile set to true to reduce memory usage when writing large zip files
    private val fileSystem: FileSystem = FileSystems.newFileSystem(
        zipFile.jarUri(),
        mapOf("create" to "true", "useTempFile" to true)
    )

    override fun close() = fileSystem.close()

    override fun bufferedWriter(filepath: String): BufferedWriter {
        val path = fileSystem.getPath(filepath)
        path.createParentDirectories()
        return Files.newBufferedWriter(path)
    }

    override fun copyDirectory(source: File, destination: String, filter: (String) -> Boolean) {
        val sourcePath = source.toPath()
        val destPath = fileSystem.getPath(destination)
        sourcePath.copyDirectoryTo(destPath, filter)
    }

    override fun copyFile(source: File, destination: String) {
        val sourcePath = source.toPath()
        val destPath = fileSystem.getPath(destination)
        if (Files.isRegularFile(sourcePath)) {
            sourcePath.copyFileTo(destPath)
        }
    }
}
