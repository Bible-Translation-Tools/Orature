package org.wycliffeassociates.otter.jvm.workbookapp.io.zip

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.wycliffeassociates.otter.common.utils.mapNotNull
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
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
        Files.copy(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING)
    }

    return pairsOfFilesToCopy.map { (_, toFile) -> toFile.toString() }
}

/** Copy a File, possibly to another [java.nio.file.FileSystem] */
internal fun Path.copyFile(dest: Path): Observable<String> {
    val sourceRoot = toAbsolutePath().parent
    val fromFile = this
    val relativePath = sourceRoot.relativize(fromFile).toString()
    val toFile = dest.resolve(relativePath)
    toFile.createParentDirectories()
    val pairsOfFilesToCopy = Observable.just(Pair(fromFile, toFile)).cache()
    pairsOfFilesToCopy.forEach { (fromFile, toFile) ->
        toFile.createParentDirectories()
        Files.copy(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING)
    }
    return pairsOfFilesToCopy.map { (_, toFile) -> toFile.toString() }
}

