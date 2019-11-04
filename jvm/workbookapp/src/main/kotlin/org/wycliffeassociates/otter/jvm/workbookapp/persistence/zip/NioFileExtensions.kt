package org.wycliffeassociates.otter.jvm.workbookapp.persistence.zip

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

/**
 *  Create a Jar:File: URI.
 *  See https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
 */
internal fun File.jarUri() = toURI().run { URI("jar:" + scheme, path, null) }

/** If this file's parent directories don't exist, create them. */
internal fun Path.createParentDirectories() = parent?.let { Files.createDirectories(it) }

/** Recursively copy a directory, possibly to another [java.nio.file.FileSystem], with per-file filter predicate. */
internal fun Path.copyDirectoryTo(dest: Path, filter: (String) -> Boolean) {
    Files.walk(this)
        .filter { Files.isRegularFile(it) }
        .forEach { fromFile ->
            val relativePath = relativize(fromFile).toString()
            if (filter(relativePath)) {
                val toFile = dest.resolve(relativePath)
                toFile.createParentDirectories()
                Files.copy(fromFile, toFile)
            }
        }
}
