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
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.LanguageEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File
import kotlin.io.path.createTempDirectory

class LanguageDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-language-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.languageDao }

    private val languages = TestDataStore.languages
    private val directoryProvider = mockk<IDirectoryProvider> {
        every { tempDirectory } returns createTempDirectory().toFile().apply { deleteOnExit() }
    }

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile, directoryProvider)
        dao.insertAll(
            TestDataStore.languages.map { LanguageMapper().mapToEntity(it) }
        )
    }

    @After
    fun cleanUp() {
        dao.fetchAll().forEach(dao::delete)
        database.close()
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(languages.size, dao.fetchAll().size)
        dao.insert(
            LanguageEntity(
                id = 0,
                slug = "test",
                name = "test",
                anglicizedName = "test",
                direction = "test",
                gateway = 0,
                region = "test"
            )
        )
        Assert.assertEquals(languages.size + 1, dao.fetchAll().size)
    }

    @Test
    fun testInsertThrowsException() {
        try {
            val nonZeroId = 1
            dao.insert(
                LanguageMapper().mapToEntity(languages.first()).copy(
                    id = nonZeroId
                )
            )
            Assert.fail(
                "An exception is expected to throw when inserting a non-zero id."
            )
        } catch (e: InsertionException) {
            Assert.assertEquals("Entity ID is not 0", e.message)
        }

        // insert duplicate (slug)
        try {
            dao.insert(
                LanguageMapper().mapToEntity(languages.first())
            )
            dao.insert(
                LanguageMapper().mapToEntity(languages.first())
            )
            Assert.fail(
                "An exception is expected to throw when inserting duplicated slug."
            )
        } catch (e: DataAccessException) { }
    }

    @Test
    fun testFetchGateway() {
        val gwLanguages = dao.fetchGateway()
        Assert.assertEquals(2, gwLanguages.size)
        Assert.assertTrue(gwLanguages.all { it.gateway == 1 })
    }

    @Test
    fun testFetchTargets() {
        val targetLanguages = dao.fetchTargets()
        Assert.assertEquals(3, targetLanguages.size)
        Assert.assertTrue(targetLanguages.all { it.gateway == 0 })
    }

    @Test
    fun testFetchBySlug() {
        val en = LanguageMapper().mapToEntity(languages[1])
        val resultEntity = dao.fetchBySlug(en.slug)

        Assert.assertNotNull(resultEntity)
        Assert.assertEquals(
            en.copy(id = resultEntity!!.id),
            resultEntity
        )

        Assert.assertNull(dao.fetchBySlug("slug-not-exist"))
    }

    @Test
    fun testFetchById() {
        Assert.assertNotNull(dao.fetchById(1))
        Assert.assertNull(dao.fetchById(999))
    }

    @Test
    fun testUpdateLanguage() {
        val old = dao.fetchBySlug("en")!!
        val updated = old.copy(
            slug = "new-en-slug",
            name = "updated-name",
            anglicizedName = "updated-anglicize-name",
            direction = "rtl",
            gateway = 0,
            region = "New Region"
        )

        dao.update(updated)
        val result = dao.fetchById(old.id)

        Assert.assertEquals(updated, result)
    }

    @Test
    fun testUpdateLanguageThrowsException() {
        val aa = languages.find { it.slug == "ar" }!!
        val entity = dao.fetchBySlug("en")!!

        val duplicated = entity.copy(
            slug = aa.slug
        )

        try {
            dao.update(duplicated)
            Assert.fail("An exception is expected to throw when setting a duplicated language slug. ")
        } catch (e: DataAccessException) { }
    }

    @Test
    fun testDeleteLanguage() {
        val entity = dao.fetchAll().first()
        dao.delete(entity)
        Assert.assertEquals(languages.size - 1, dao.fetchAll().size)
    }
}