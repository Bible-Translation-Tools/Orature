/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package integrationtest.projects.importer

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import javax.inject.Inject

class TestRCImporterFactory {
    @Inject
    lateinit var factory: RCImporterFactory

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun testMakeImporter() {
        val importer = factory.makeImporter()

        Assert.assertTrue(importer is OngoingProjectImporter)
        Assert.assertTrue(importer.getNext() is ExistingSourceImporter)
        Assert.assertTrue(importer.getNext()!!.getNext() is NewSourceImporter)
    }
}
