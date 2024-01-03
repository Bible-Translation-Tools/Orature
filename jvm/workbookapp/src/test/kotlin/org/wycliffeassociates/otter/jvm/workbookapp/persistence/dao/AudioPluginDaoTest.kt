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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.jooq.exception.DataAccessException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.AudioPluginEntity
import java.io.File

class AudioPluginDaoTest {
    private val testDatabaseFile =
        File.createTempFile(
            "test-audio-plugin-dao",
            ".sqlite",
        ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.audioPluginDao }

    private val defaultEnity =
        AudioPluginEntity(
            id = 0,
            name = "test",
            version = "test",
            bin = "test-jar",
            args = "test",
            edit = 0,
            record = 0,
            mark = 0,
            path = null,
        )

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(0, dao.fetchAll().size)
        val id1 = dao.insert(defaultEnity)
        Assert.assertEquals(1, dao.fetchAll().size)

        // insert the same entity will trigger an update
        val id2 = dao.insert(defaultEnity)

        Assert.assertEquals(
            "Inserting a duplicate should update the existing record and not increase the total",
            1,
            dao.fetchAll().size,
        )
        Assert.assertEquals(id1, id2)
    }

    @Test
    fun testInsertThrowsException() {
        try {
            dao.insert(defaultEnity.copy(id = 1))
            Assert.fail("Inserting nonzero id should throw an exception")
        } catch (e: InsertionException) {
        }
    }

    @Test
    fun testFetchById() {
        val insertedId = dao.insert(defaultEnity)
        val entity = dao.fetchById(insertedId)

        Assert.assertNotNull(entity)
        Assert.assertEquals(
            "Fetched record should match the given object",
            defaultEnity.copy(id = insertedId),
            entity,
        )
    }

    @Test
    fun testUpdate() {
        val insertedId = dao.insert(defaultEnity)
        val updated =
            AudioPluginEntity(
                id = insertedId,
                name = "updated",
                version = "updated",
                bin = "updated",
                args = "updated",
                edit = 1,
                record = 1,
                mark = 1,
                path = "updated",
            )

        dao.update(updated)

        Assert.assertEquals(
            "Updated record should match the given value",
            updated,
            dao.fetchById(insertedId),
        )
    }

    @Test
    fun testUpdateDuplicatedFieldsThrowsException() {
        val existingId = dao.insert(defaultEnity)
        val existingEntity = dao.fetchById(existingId)!!

        val newId =
            dao.insert(
                AudioPluginEntity(
                    id = 0,
                    name = "duplicate",
                    version = "duplicate",
                    bin = "duplicate",
                    args = "duplicate",
                    edit = 1,
                    record = 1,
                    mark = 1,
                    path = "duplicate",
                ),
            )
        val newEntity = dao.fetchById(newId)!!

        Assert.assertEquals(2, dao.fetchAll().size)

        // update new entity with the values of existing entity
        try {
            dao.update(
                newEntity.copy(
                    name = existingEntity.name,
                    version = existingEntity.version,
                ),
            )

            Assert.fail("Update to the existing values of name and version should throw an exception")
        } catch (e: DataAccessException) {
        }
    }

    @Test
    fun testDelete() {
        val insertedId = dao.insert(defaultEnity)
        Assert.assertEquals(1, dao.fetchAll().size)

        dao.delete(defaultEnity.copy(insertedId))

        Assert.assertNull(dao.fetchById(insertedId))
        Assert.assertEquals(0, dao.fetchAll().size)
    }
}
