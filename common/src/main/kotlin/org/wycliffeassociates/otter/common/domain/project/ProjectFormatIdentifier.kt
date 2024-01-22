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

import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.InvalidObjectException
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

object ProjectFormatIdentifier {

    @Throws(
        IllegalArgumentException::class,
        InvalidResourceContainerException::class
    )
    fun getProjectFormat(
        file: File
    ): ProjectFormat {
        return when {
            OratureFileFormat.isSupported(file.extension) || file.isDirectory -> {
                validateOratureFile(file)
                ProjectFormat.RESOURCE_CONTAINER
            }
            else -> {
                throw IllegalArgumentException("The following file is not supported: $file")
            }
        }
    }

    private fun validateOratureFile(file: File) {
        try {
            ResourceContainer.load(file).close()
        } catch (e: Exception) {
            throw InvalidResourceContainerException("Invalid resource container file $file")
        }
    }
}

class InvalidResourceContainerException(
    override val message: String
) : InvalidObjectException(message)