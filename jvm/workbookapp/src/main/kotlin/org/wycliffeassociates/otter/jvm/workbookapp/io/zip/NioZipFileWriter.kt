package org.wycliffeassociates.otter.jvm.workbookapp.io.zip

import org.wycliffeassociates.otter.common.io.zip.IZipFileWriter
import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

class NioZipFileWriter(
    zipFile: File
) : IZipFileWriter {
    private val fileSystem: FileSystem = FileSystems.newFileSystem(zipFile.jarUri(), mapOf("create" to "true"))

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