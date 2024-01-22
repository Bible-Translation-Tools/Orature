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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.exception.DataAccessException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File

class ResourceMetadataDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-resource-metadata-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.resourceMetadataDao }

    companion object {
        val sampleEntities = getTestSampleEntities()

        fun getTestSampleEntities(): List<ResourceMetadataEntity> {
            val file = File(
                javaClass.classLoader.getResource("resource-metadata-entity-samples.json").file
            )

            return jacksonObjectMapper()
                .readValue(file.readText())
        }
    }

    @Before
    fun setUp() {
        database = AppDatabase(testDatabaseFile)
        seedLanguages()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(0, dao.fetchAll().size)
        sampleEntities.forEach { dao.insert(it) }
        Assert.assertEquals(sampleEntities.size, dao.fetchAll().size)
    }

    @Test
    fun testFetch() {
        sampleEntities.forEach {
            dao.insert(it)

            val entity = dao.fetch(
                languageId = it.languageFk,
                identifier = it.identifier,
                version = it.version,
                creator = it.creator
            )
            Assert.assertNotNull(entity)
        }
    }

    @Test
    fun testFetchById() {
        Assert.assertNull(dao.fetchById(1))
        dao.insert(sampleEntities[0])
        Assert.assertNotNull(dao.fetchById(1))
    }

    @Test
    fun testFetchLatestVersion() {
        sampleEntities.forEach {
            dao.insert(it)
        }

        var entity = dao.fetchLatestVersion(
            languageSlug = "en",
            identifier = "ulb"
        )
        Assert.assertNotNull(entity)

        entity = dao.fetchLatestVersion(
            languageSlug = "en",
            identifier = "ulb",
            creator = "non existing",
            derivedFromFk = null,
            relaxCreatorIfNoMatch = true
        )
        Assert.assertNotNull(entity)

        entity = dao.fetchLatestVersion(
            languageSlug = "en",
            identifier = "ulb",
            creator = "non existing",
            derivedFromFk = null,
            relaxCreatorIfNoMatch = false
        )
        Assert.assertNull(entity)
    }

    @Test
    fun testAddLink() {
        sampleEntities.forEach {
            dao.insert(it)
        }

        dao.addLink(1,2)
        var links = dao.fetchLinks(1)

        Assert.assertEquals(1, links.size)
        Assert.assertEquals(0, dao.fetchLinks(3).size)

        // add non existing link does not crash
        dao.addLink(999, 1000)
        links = dao.fetchLinks(999)

        Assert.assertEquals(0, links.size)
    }

    @Test
    fun testRemoveLink() {
        sampleEntities.forEach {
            dao.insert(it)
        }

        dao.addLink(1,2)
        var links = dao.fetchLinks(1)

        Assert.assertEquals(1, links.size)

        dao.removeLink(1, 2)
        links = dao.fetchLinks(1)

        Assert.assertEquals(0, links.size)

        // remove non existing link does not crash
        dao.removeLink(999, 1000)
    }

    @Test
    fun testUpdate() {
        sampleEntities.forEach {
            dao.insert(it)
        }

        val entity = dao.fetchById(1)!!
        val updatedEntity = ResourceMetadataEntity(
            conformsTo = "test",
            creator = "test",
            description = "test",
            format = "test",
            identifier = "test",
            issued = "test",
            languageFk = 1,
            modified = "test",
            publisher = "test",
            subject = "test",
            type = "test",
            title = "test",
            version = "test",
            license = "test",
            path = "test",
            derivedFromFk = null,
            id = entity.id
        )

        dao.update(updatedEntity)
        val result = dao.fetchById(entity.id)

        Assert.assertEquals(updatedEntity, result)
    }

    @Test
    fun testDelete() {
        sampleEntities.forEach {
            dao.insert(it)
        }
        val entity = dao.fetchById(1)!!
        try {
            dao.delete(entity)
            Assert.fail("Deleting resource metadata with FK constraint should throw an exception")
        } catch (e: DataAccessException) {
            // not deleted
            Assert.assertNotNull(dao.fetchById(1))
        }

        Assert.assertEquals(sampleEntities.size, dao.fetchAll().size)

        fun fetchEntity() = dao.fetchById(4)
        dao.delete(fetchEntity()!!)

        Assert.assertNull(fetchEntity())
        Assert.assertEquals(
            "After deleting, the total number should decrease by 1.",
            sampleEntities.size - 1,
            dao.fetchAll().size
        )
    }

    private fun seedLanguages() {
        database.languageDao
            .insertAll(
                TestDataStore.languages.map {
                    LanguageMapper().mapToEntity(it)
                }
            )
    }
}