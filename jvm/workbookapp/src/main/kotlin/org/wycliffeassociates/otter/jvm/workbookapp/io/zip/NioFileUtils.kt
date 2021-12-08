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
internal fun Path.copyFileTo(dest: Path): Observable<String> {
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