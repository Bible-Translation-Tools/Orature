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

import org.bibletranslationtools.scriptureburrito.container.BurritoContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.BurritoToResourceContainerConverter
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.tstudio2rc.Tstudio2RcConverter
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

object ProjectFormatIdentifier {

    private val projectFormatIdentifier: IFormatIdentifier
        get() {
            // set up the chains for identifying the project format
            val orature = OratureFileIdentifier()
            val tstudio = TstudioFileIdentifier()
            val burrito = ScriptureBurritoFileIdentifier()

            orature.next = tstudio
            tstudio.next = burrito

            return orature
        }

    @Throws(IllegalArgumentException::class)
    fun getProjectFormat(
        file: File
    ): ProjectFormat {
        return projectFormatIdentifier.getFormat(file)
            ?: throw IllegalArgumentException("The following file is not supported: $file")
    }

    /**
     * Chains of Responsibility - getting the corresponding format of a given project file
     */
    private interface IFormatIdentifier {
        var next: IFormatIdentifier?

        /**
         * Returns the project format of the given file
         */
        fun getFormat(file: File): ProjectFormat?
    }

    private class OratureFileIdentifier : IFormatIdentifier {

        override var next: IFormatIdentifier? = null

        override fun getFormat(file: File): ProjectFormat? {
            return try {
                ResourceContainer.load(file).close()
                ProjectFormat.RESOURCE_CONTAINER
            } catch (e: Exception) {
                next?.getFormat(file)
            }
        }
    }
    private class TstudioFileIdentifier : IFormatIdentifier {

        override var next: IFormatIdentifier? = null

        override fun getFormat(file: File): ProjectFormat? {
            return if (Tstudio2RcConverter.isValidFormat(file)) {
                ProjectFormat.TSTUDIO
            } else {
                next?.getFormat(file)
            }
        }
    }

    private class ScriptureBurritoFileIdentifier : IFormatIdentifier {
        override var next: IFormatIdentifier? = null

        override fun getFormat(file: File): ProjectFormat? {
            return try {
                BurritoContainer.load(file).close()
                ProjectFormat.SCRIPTURE_BURRITO
            } catch (e: Exception) {
                next?.getFormat(file)
            }
        }
    }
}