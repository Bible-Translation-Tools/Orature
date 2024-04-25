/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.utils

import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

const val SELECTED_TAKES_FROM_DB = "selectedTakesInDatabase.txt"
/**
 *  Returns a new file path with the suffix appended to the file name
 *  (file name here does not include the extension).
 *
 *  @param path the original file path.
 *  @param suffix the suffix to append to the file name excluding the extension.
 */
fun filePathWithSuffix(path: String, suffix: String): String {
    val file = File(path)
    return file
        .parentFile
        .resolve(file.nameWithoutExtension + suffix + ".${file.extension}")
        .invariantSeparatorsPath
}

/**
 * Returns the MD5 checksum of the given file or null if the file doesn't exist.
 */
fun computeFileChecksum(file: File): String? {
    if (!file.exists()) {
        return null
    }
    val md = MessageDigest.getInstance("MD5")

    FileInputStream(file).use { inputStream ->
        DigestInputStream(inputStream, md).use { digestInputStream ->
            val buffer = ByteArray(8192)
            while (digestInputStream.read(buffer) != -1) {
                // Continue reading the file
            }
        }
    }

    val md5Bytes = md.digest()
    val md5Checksum = StringBuilder()
    for (byte in md5Bytes) {
        md5Checksum.append(String.format("%02x", byte))
    }

    return md5Checksum.toString()
}