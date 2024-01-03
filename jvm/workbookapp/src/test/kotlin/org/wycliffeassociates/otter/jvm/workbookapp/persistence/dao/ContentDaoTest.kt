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

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.ContentTypeDao
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.RecordMappers
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ContentMapper
import java.io.File

class ContentDaoTest {
    private val testDatabaseFile =
        File.createTempFile(
            "test-content-dao",
            ".sqlite",
        ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.contentDao }

    private lateinit var defaultEntity: ContentEntity
    private var defaultCollectionId = 999

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
        database.dsl.execute("PRAGMA foreign_keys = OFF;")

        defaultEntity =
            ContentMapper(database.contentTypeDao)
                .mapToEntity(TestDataStore.content.first())
                .copy(
                    id = 0,
                    collectionFk = defaultCollectionId,
                    selectedTakeFk = null,
                )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(0, dao.fetchAll().size)
        insertDefault()
        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testInsertNoReturn() {
        Assert.assertEquals(0, dao.fetchAll().size)
        dao.insertNoReturn(defaultEntity)
        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testInsertThrowsException() {
        try {
            dao.insert(defaultEntity.copy(id = 1))
            Assert.fail("Insert entity must have zero id")
        } catch (e: InsertionException) {
        }

        try {
            dao.insertNoReturn(defaultEntity.copy(id = 1))
            Assert.fail("Insert entity must have zero id")
        } catch (e: InsertionException) {
        }
    }

    @Test
    fun testFetchByCollectionId() {
        insertDefault()
        val result = dao.fetchByCollectionId(defaultCollectionId)

        Assert.assertEquals(1, result.size)
    }

    @Test
    fun testFetchByCollectionIdAndType() {
        insertDefault()
        val result = dao.fetchByCollectionIdAndType(defaultCollectionId, ContentType.TEXT)
        val emptyResult = dao.fetchByCollectionIdAndType(defaultCollectionId, ContentType.BODY)

        Assert.assertEquals(
            "There should be one matching result with the given id and type.",
            1,
            result.size,
        )
        Assert.assertEquals(
            "There should be no match for the given id and type.",
            0,
            emptyResult.size,
        )
    }

    @Test
    fun testFetchAndUpdateSources() {
        insertAllSamples()
        Assert.assertEquals(3, dao.fetchAll().size)

        val chapterContentTypeId = ContentTypeDao(database.dsl).fetchId(ContentType.META)
        val verseContentTypeId = ContentTypeDao(database.dsl).fetchId(ContentType.TEXT)
        val chapter = dao.fetchAll().first { it.type_fk == chapterContentTypeId }
        val content = dao.fetchAll().first { it.type_fk == verseContentTypeId }

        Assert.assertEquals(0, dao.fetchSources(content).size)

        dao.updateSources(content, listOf(chapter))
        val result = dao.fetchSources(content)

        Assert.assertEquals(
            "After updating sources, there should be one record.",
            1,
            result.size,
        )
        Assert.assertEquals(chapter, result.first())
    }

    @Test
    fun testFetchContentByProjectSlug() {
        val projectSlug = CollectionDaoTest.defaultCollection.slug
        val projectId =
            database.collectionDao.insert(
                CollectionDaoTest.defaultCollection,
            )
        val chapterCollectionId =
            database.collectionDao
                .insert(
                    CollectionDaoTest.defaultCollection
                        .copy(
                            slug = "chapter-test",
                            title = "chapter-test",
                            label = "chapter-test",
                            sort = 2,
                            parentFk = projectId,
                        ),
                )
        dao.insert(
            defaultEntity.copy(
                collectionFk = chapterCollectionId,
            ),
        )

        val contents =
            dao.fetchContentByProjectSlug(projectSlug).fetch {
                RecordMappers.mapToContentEntity(it)
            }

        Assert.assertEquals(
            "There should be one content record for the project: $projectSlug",
            1,
            contents.size,
        )
        Assert.assertEquals(
            "Fetched collection FK should match the given chapter collection id",
            chapterCollectionId,
            contents.first().collectionFk,
        )
    }

    @Test
    fun testDelete() {
        insertDefault()
        Assert.assertEquals(1, dao.fetchAll().size)
        dao.delete(defaultEntity.copy(id = 1))
        Assert.assertEquals(0, dao.fetchAll().size)
    }

    @Test
    fun testDeleteForCollection() {
        insertAllSamples()

        val newCollectionId =
            database.collectionDao
                .insert(
                    CollectionDaoTest.defaultCollection
                        .copy(
                            slug = "second-collection",
                        ),
                )
        dao.insert(
            defaultEntity.copy(
                id = 0,
                collectionFk = newCollectionId,
            ),
        )

        Assert.assertEquals(4, dao.fetchAll().size)

        val collection = database.collectionDao.fetchById(newCollectionId)
        dao.deleteForCollection(collection)

        Assert.assertEquals(
            "After deleting content for collection, the remaining total should decrease by 1.",
            3,
            dao.fetchAll().size,
        )
    }

    private fun insertDefault(): Int {
        return dao.insert(defaultEntity)
    }

    private fun insertAllSamples() {
        val mapper = ContentMapper(database.contentTypeDao)

        TestDataStore.content.forEach {
            val contentEntity =
                mapper.mapToEntity(it)
                    .copy(
                        id = 0,
                        collectionFk = 9999,
                        selectedTakeFk = null,
                    )
            dao.insert(contentEntity)
        }
    }
}
