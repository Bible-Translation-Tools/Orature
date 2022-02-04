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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.io.BufferedReader
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class OtterZipFile(
    val absolutePath: String,
    private val rootZipFile: ZipFile,
    private val separator: String,
    val rootPathWithinZip: String?,
    val parentFile: OtterFile? = null,
    private val zipEntry: ZipEntry? = null
) {
    val isFile = !(zipEntry == null || zipEntry.isDirectory)
    val name: String = File(absolutePath).name
    val nameWithoutExtension = File(absolutePath).nameWithoutExtension

    fun bufferedReader(): BufferedReader = rootZipFile.getInputStream(zipEntry).bufferedReader()

    fun toRelativeString(parent: OtterFile): String {
        val suffixTrimmed = absolutePath.removeSuffix(separator)
        val prefixesToTrim = listOfNotNull(parent.absolutePath, rootPathWithinZip)
            .flatMap { listOf(it, ".$separator", separator) }
        return prefixesToTrim.fold(suffixTrimmed, String::removePrefix)
    }

    companion object {
        fun otterFileZ(
            absolutePath: String,
            rootZipFile: ZipFile,
            separator: String,
            rootPathWithinZip: String?,
            parentFile: OtterFile? = null
        ): OtterFile {
            val zipEntry = rootZipFile.getEntry(absolutePath)
            return OtterFile.Z(
                OtterZipFile(
                    absolutePath = absolutePath,
                    rootZipFile = rootZipFile,
                    separator = separator,
                    rootPathWithinZip = rootPathWithinZip,
                    parentFile = parentFile,
                    zipEntry = zipEntry
                )
            )
        }
    }
}
