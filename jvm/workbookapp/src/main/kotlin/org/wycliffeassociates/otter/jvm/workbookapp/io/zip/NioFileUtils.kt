package org.wycliffeassociates.otter.jvm.workbookapp.io.zip

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.wycliffeassociates.otter.common.utils.mapNotNull
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

/**
 *  Create a Jar:File: URI.
 *  See https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
 */
internal fun File.jarUri() = toURI().run { URI("jar:" + scheme, path, null) }

/** If this file's parent directories don't exist, create them. */
internal fun Path.createParentDirectories() = parent?.let { Files.createDirectories(it) }

/** Recursively copy a directory, possibly to another [java.nio.file.FileSystem], with per-file filter predicate. */
internal fun Path.copyDirectoryTo(dest: Path, filter: (String) -> Boolean): Observable<String> {
    val sourceRoot = toAbsolutePath()
    val pairsOfFilesToCopy = Files.walk(sourceRoot)
        .asSequence()
        .toObservable()
        .filter { Files.isRegularFile(it) }
        .mapNotNull { fromFile ->
            val relativePath = sourceRoot.relativize(fromFile).toString()
            if (filter(relativePath)) {
                val toFile = dest.resolve(relativePath)
                fromFile to toFile
            } else null
        }
        .cache()

    pairsOfFilesToCopy.forEach { (fromFile, toFile) ->
        toFile.createParentDirectories()
        Files.copy(fromFile, toFile)
    }

    return pairsOfFilesToCopy.map { (_, toFile) -> toFile.toString() }
}
