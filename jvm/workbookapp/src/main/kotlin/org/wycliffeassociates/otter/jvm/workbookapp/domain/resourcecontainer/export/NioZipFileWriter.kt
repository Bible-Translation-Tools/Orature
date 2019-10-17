package org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.export

import org.wycliffeassociates.otter.common.domain.resourcecontainer.export.IZipFileWriter
import java.io.BufferedWriter
import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class NioZipFileWriter(
    zipFile: File
) : IZipFileWriter {
    private val fileSystem: FileSystem = FileSystems.newFileSystem(zipFile.jarUri(), mapOf("create" to "true"))

    override fun close() = fileSystem.close()

    override fun bufferedWriter(filepath: String): BufferedWriter {
        val path = fileSystem.getPath(filepath)
        path.parent?.let { Files.createDirectories(it) }
        return Files.newBufferedWriter(path)
    }

    override fun copyDirectory(source: File, destination: String) {
        val sourcePath = source.toPath()
        val destPath = fileSystem.getPath(destination)
        Files.walk(sourcePath)
            .forEach {
                Files.copy(it, correspondingPath(it, sourcePath, destPath))
            }
    }

    private fun correspondingPath(
        path: Path,
        oldRoot: Path,
        newRoot: Path
    ) = newRoot.resolve(oldRoot.relativize(path))

    private fun File.jarUri(): URI {
        // https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
        val fileUri = toURI()
        return URI("jar:" + fileUri.scheme, fileUri.path, null)
    }
}