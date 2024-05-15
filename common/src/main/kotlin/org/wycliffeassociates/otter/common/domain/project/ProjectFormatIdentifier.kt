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
package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.tstudio2rc.Converter
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

object ProjectFormatIdentifier {

    private val projectFormatIdentifier: ProjectFormatIdentifier
        get() {
            val orature = OratureFileValidator()
            val tstudio = TstudioFileValidator()
            orature.next = tstudio
            return orature
        }

    @Throws(IllegalArgumentException::class)
    fun getProjectFormat(
        file: File
    ): ProjectFormat {
        return projectFormatIdentifier.getFormat(file)
            ?: throw IllegalArgumentException("The following file is not supported: $file")
    }

    private interface ProjectFormatIdentifier {
        var next: ProjectFormatIdentifier?
        fun getFormat(file: File): ProjectFormat?
    }

    private class OratureFileValidator : ProjectFormatIdentifier {

        override var next: ProjectFormatIdentifier? = null

        override fun getFormat(file: File): ProjectFormat? {
            return try {
                ResourceContainer.load(file).close()
                ProjectFormat.RESOURCE_CONTAINER
            } catch (e: Exception) {
                next?.getFormat(file)
            }
        }
    }
    private class TstudioFileValidator : ProjectFormatIdentifier {

        override var next: ProjectFormatIdentifier? = null

        override fun getFormat(file: File): ProjectFormat? {
            return if (Converter.isValidFormat(file)) {
                ProjectFormat.TSTUDIO
            } else {
                next?.getFormat(file)
            }
        }
    }
}