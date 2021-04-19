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
