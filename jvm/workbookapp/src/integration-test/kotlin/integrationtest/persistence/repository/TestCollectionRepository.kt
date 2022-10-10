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
package integrationtest.persistence.repository

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import jooq.Tables.TAKE_ENTITY
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import javax.inject.Inject
import javax.inject.Provider

class TestCollectionRepository {

    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var collectionRepository: ICollectionRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()

    /**
     * Verifies deleting the resource (helper) takes of a project
     * and maintaining the Scripture takes
     */
    @Test
    fun testDeleteResource() {
        db
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .import("en-x-demo1-ulb-rev.zip")
            .import("en-x-demo1-tn-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        ContentType.META to 1211,
                        ContentType.TEXT to 31124,
                        ContentType.TITLE to 81419,
                        ContentType.BODY to 78637
                    ),
                    collections = 1279,
                    links = 157581
                )
            )

        val dsl = db.db.dsl
        var takeCount = dsl
            .select(TAKE_ENTITY.asterisk())
            .from(TAKE_ENTITY)
            .count()

        Assert.assertEquals(
            "Total takes before delete should be 6",
            6,
            takeCount
        )

        val project = collectionRepository
            .getDerivedProjects().blockingGet()
            .single()

        // delete resource takes
        collectionRepository.deleteResources(project, true).blockingAwait()
        takeCount = dsl
            .select(TAKE_ENTITY.asterisk())
            .from(TAKE_ENTITY)
            .count()

        Assert.assertEquals(
            "After deleting resource takes, the remaining total should be 3",
            3,
            takeCount
        )
    }
}