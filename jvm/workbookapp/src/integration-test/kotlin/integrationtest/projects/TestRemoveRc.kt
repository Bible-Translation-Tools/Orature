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
package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Test
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentType.META
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TEXT
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestRemoveRc {
    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject lateinit var removeUseCase: Provider<DeleteResourceContainer>

    private val logger = LoggerFactory.getLogger(javaClass)

    private val rcToDelete = "en_ulb.zip"

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val beforeDelete = RowCount(
        collections = 2512,
        contents = mapOf(
            TEXT to 62208,
            META to 2378
        ),
        links = 0
    )

    private val afterDelete = RowCount(
        collections = 1256,
        contents = mapOf(
            TEXT to 31104,
            META to 1189
        ),
        links = 0
    )

    @Test
    fun deleteRC() {
        dbEnvProvider.get()
            .import("hi_ulb.zip")
            .import(rcToDelete)
            .assertRowCounts(
                beforeDelete,
                "Row count before delete doesn't match."
            )
            .apply {
                val rc = ResourceContainer.load(
                    getResource(rcToDelete)
                )
                removeUseCase.get()
                    .delete(rc).blockingAwait()
            }
            .assertRowCounts(
                afterDelete,
                "Row count after delete doesn't match."
            )
    }

    private fun getResource(rcFile: String) =
        javaClass.classLoader
            .getResource("resource-containers/$rcFile")!!.file.let { File(it) }
}