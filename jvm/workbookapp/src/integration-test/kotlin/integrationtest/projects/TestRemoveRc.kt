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
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentType.META
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TEXT
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestRemoveRc {
    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject lateinit var removeUseCase: Provider<DeleteResourceContainer>

    @Inject lateinit var collectionRepo: ICollectionRepository

    @Inject lateinit var languageRepo: ILanguageRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    private val enUlb = "en_ulb.zip"

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun deleteRC_success() {
        dbEnvProvider.get()
            .import("hi_ulb.zip")
            .import(enUlb)
            .assertRowCounts(
                RowCount(
                    collections = 2512,
                    contents = mapOf(
                        META to 2378,
                        TEXT to 62208
                    ),
                    links = 0
                ),
                "Row count before delete doesn't match."
            )
            .apply {
                assertEquals(2, db.resourceMetadataDao.fetchAll().size)

                val rc = ResourceContainer.load(
                    getResource(enUlb)
                )
                val result = removeUseCase.get().delete(rc).blockingGet()

                assertEquals(DeleteResult.SUCCESS, result)
                assertEquals(1, db.resourceMetadataDao.fetchAll().size)
            }
            .assertRowCounts(
                RowCount(
                    collections = 1256,
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31104
                    ),
                    links = 0
                ),
                "Row count after delete doesn't match."
            )
    }

    @Test
    fun `deleteRC aborted when derived project exists`() {
        dbEnvProvider.get()
            .import(enUlb)
            .import("hi_ulb.zip")
            .assertRowCounts(
                RowCount(
                    collections = 2512,
                    contents = mapOf(
                        META to 2378,
                        TEXT to 62208
                    ),
                    links = 0
                ),
                "Row count before delete doesn't match."
            )
            .apply {
                assertEquals(2, db.resourceMetadataDao.fetchAll().size)

                val language = languageRepo.getBySlug("en").blockingGet()
                createProject(
                    collectionRepo
                        .getRootSources().blockingGet()
                        .first { it.resourceContainer?.language == language },
                    language
                )
                val rc = ResourceContainer.load(
                    getResource(enUlb)
                )
                val result = removeUseCase.get().delete(rc).blockingGet()

                assertEquals(DeleteResult.DEPENDENCY_EXISTS, result)
            }
            .assertRowCounts(
                RowCount(
                    collections = 2579,
                    contents = mapOf(
                        META to 2378,
                        TEXT to 62208
                    ),
                    links = 0
                )
            )
    }

    @Test
    fun `deleteRC success when existing rc has different version`() {
        dbEnvProvider.get()
            .import(enUlb)
            .import("en_ulb_newer_ver.zip")
            .assertRowCounts(
                RowCount(
                    collections = 2512,
                    contents = mapOf(
                        META to 2378,
                        TEXT to 62208
                    ),
                    links = 0
                ),
                "Row count before delete doesn't match."
            )
            .apply {
                val rc = ResourceContainer.load(
                    getResource(enUlb)
                )
                val result = removeUseCase.get().delete(rc).blockingGet()
            }
    }

    private fun getResource(rcFile: String) =
        javaClass.classLoader.getResource(
            "resource-containers/$rcFile"
        )!!.file.let { File(it) }
}
