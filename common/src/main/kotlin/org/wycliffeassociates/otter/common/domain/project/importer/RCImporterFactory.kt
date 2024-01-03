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
package org.wycliffeassociates.otter.common.domain.project.importer

import javax.inject.Inject
import javax.inject.Provider

class RCImporterFactory @Inject constructor() : IProjectImporterFactory {

    @Inject lateinit var ongoingProjectImporter: Provider<OngoingProjectImporter>
    @Inject lateinit var existingProjectImporter: Provider<ExistingSourceImporter>
    @Inject lateinit var newSourceImporter: Provider<NewSourceImporter>

    /**
     * Constructed using the Chain of Responsibility (COR) pattern.
     * Each importer has a "pointer" to the next importer to which it can pass
     * the request for subsequent processing if needed.
     */
    private val importer: RCImporter by lazy {
        val importer1 = ongoingProjectImporter.get()
        val importer2 = existingProjectImporter.get()
        val importer3 = newSourceImporter.get()

        importer1.setNext(importer2)
        importer2.setNext(importer3)

        importer1
    }

    override fun makeImporter(): RCImporter = importer
}