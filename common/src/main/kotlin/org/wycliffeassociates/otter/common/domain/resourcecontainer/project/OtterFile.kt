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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.io.BufferedReader
import java.io.File

sealed class OtterFile {
    class F(val f: File) : OtterFile()
    class Z(val z: OtterZipFile) : OtterFile()

    companion object {
        fun otterFileF(f: File): OtterFile {
            return OtterFile.F(f)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is OtterFile -> backingFile == other.backingFile
            else -> false
        }
    }

    private val backingFile: Any
        get() = when (this) {
            is F -> f
            is Z -> z
        }

    val nameWithoutExtension: String
        get() = when (this) {
            is F -> f.nameWithoutExtension
            is Z -> z.nameWithoutExtension
        }

    val isFile: Boolean
        get() = when (this) {
            is F -> f.isFile
            is Z -> z.isFile
        }

    val parentFile: OtterFile?
        get() = when (this) {
            is F -> OtterFile.F(f.parentFile)
            is Z -> z.parentFile
        }

    val name: String
        get() = when (this) {
            is F -> f.name
            is Z -> z.name
        }

    val absolutePath: String
        get() = when (this) {
            is F -> f.absolutePath
            is Z -> z.absolutePath
        }

    fun bufferedReader(): BufferedReader = when (this) {
        is F -> f.bufferedReader()
        is Z -> z.bufferedReader()
    }

    fun toRelativeString(parentFile: OtterFile): String = when (this) {
        // Note: This cast should throw an exception if the cast fails because we should not
        // have a file with a parent file that is a zip file
        is F -> this.f.toRelativeString((parentFile as F).f)
        // We can have a zip file whose parent file is a file (directory)
        is Z -> this.z.toRelativeString(parentFile)
    }
}