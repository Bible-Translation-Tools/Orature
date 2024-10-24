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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import io.mockk.every
import io.mockk.mockk
import org.jooq.exception.DataAccessException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import java.io.File
import kotlin.io.path.createTempDirectory

class CollectionDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-collection-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.collectionDao }
    private val directoryProvider = mockk<IDirectoryProvider> {
        every { tempDirectory } returns createTempDirectory().toFile().apply { deleteOnExit() }
    }

    companion object {
        val defaultCollection = CollectionEntity(
            id = 0,
            parentFk = null,
            sourceFk = null,
            label = "test",
            title = "test",
            slug = "test",
            sort = 1,
            dublinCoreFk = 1,
            modifiedTs = null
        )
    }

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile, directoryProvider)
        database.dsl.execute("PRAGMA foreign_keys = OFF;")
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun testInsertCollection() {
        Assert.assertEquals(0, dao.fetchAll().size)

        insertDefaultCollection()

        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testInsertThrowsException() {
        try {
            dao.insert(
                defaultCollection.copy(dublinCoreFk = null)
            )
            Assert.fail("DublinCore FK must not be null.")
        } catch (e: DataAccessException) { }

        try {
            insertDefaultCollection()
            insertDefaultCollection()
            Assert.fail("Insert duplicated value for unique field should throw an exception.")
        } catch (e: DataAccessException) { }

        try {
            dao.insert(
                defaultCollection.copy(id = 1)
            )
            Assert.fail("Insert nonzero id should throw an exception.")
        } catch (e: InsertionException) { }
    }

    @Test
    fun testFetch() {
        insertDefaultCollection()
        val result = dao.fetch(
            defaultCollection.slug,
            defaultCollection.dublinCoreFk!!,
            defaultCollection.label
        )

        Assert.assertNotNull(result)
        Assert.assertEquals(
            "Fetched record should match the given value",
            defaultCollection.copy(id = result!!.id),
            result
        )
    }

    @Test
    fun testFetchByLabel() {
        insertDefaultCollection()
        val result = dao.fetchByLabel(defaultCollection.label)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(defaultCollection.label, result.first().label)
    }

    @Test
    fun testFetchChildren() {
        val entityId = insertDefaultCollection()
        val entity = dao.fetchById(entityId)
        val child1 = entity.copy(id = 0, parentFk = entity.id, slug = "child-1")
        val child2 = entity.copy(id = 0, parentFk = entity.id, slug = "child-2")

        dao.insert(child1)
        dao.insert(child2)
        val children = dao.fetchChildren(entity)

        Assert.assertEquals(2, children.size)

        val firstChild = children.find { it.slug == child1.slug }
        val secondChild = children.find { it.slug == child2.slug }
        Assert.assertNotNull(
            "First child should be found in fetched result",
            firstChild
        )
        Assert.assertNotNull(
            "Second child should be found in fetched result",
            secondChild
        )
    }

    @Test
    fun testFetchSource() {
        val entityId = insertDefaultCollection()
        val source = dao.fetchById(entityId)
        val target = source.copy(id = 0, sourceFk = source.id, slug = "source-collection")

        dao.insert(target)
        val result = dao.fetchSource(target)

        Assert.assertNotNull(result)
        Assert.assertEquals(source, result)
    }

    @Test
    fun testUpdate() {
        val entityId = insertDefaultCollection()
        val updated = CollectionEntity(
            parentFk = null,
            sourceFk = null,
            title = "updated",
            label = "updated",
            slug = "updated",
            sort = 1,
            modifiedTs = "updated",
            dublinCoreFk = 1,
            id = entityId
        )

        dao.update(updated)

        Assert.assertEquals(updated, dao.fetchAll().single())
    }

    @Test
    fun testDelete() {
        insertDefaultCollection()

        fun fetch() = dao.fetch(
            defaultCollection.slug, defaultCollection.dublinCoreFk!!, defaultCollection.label
        )

        var entity = fetch()
        Assert.assertNotNull(entity)
        Assert.assertEquals(1, dao.fetchAll().size)

        dao.delete(entity!!)
        entity = fetch()

        Assert.assertNull(entity)
        Assert.assertEquals(
            "After delete, there should be 0 records.",
            0,
            dao.fetchAll().size
        )
    }

    private fun insertDefaultCollection(): Int {
        return dao.insert(defaultCollection)
    }
}