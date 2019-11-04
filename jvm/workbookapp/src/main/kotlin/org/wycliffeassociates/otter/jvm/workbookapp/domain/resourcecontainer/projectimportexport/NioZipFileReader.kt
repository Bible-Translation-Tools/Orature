package org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.IZipFileReader
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

class NioZipFileReader(
    zipFile: File
) : IZipFileReader {
    private val fileSystem: FileSystem = FileSystems.newFileSystem(zipFile.jarUri(), mapOf("create" to "false"))

    override fun close() = fileSystem.close()

    override fun exists(filepath: String) = Files.exists(fileSystem.getPath(filepath))

    override fun bufferedReader(filepath: String) = Files.newBufferedReader(fileSystem.getPath(filepath))!!

    override fun copyDirectory(source: String, destinationDirectory: File, filter: (String) -> Boolean) {
        val sourcePath = fileSystem.getPath(source)
        val destPath = destinationDirectory.toPath()
        sourcePath.copyDirectoryToOtherFilesystem(destPath, filter)
    }
}