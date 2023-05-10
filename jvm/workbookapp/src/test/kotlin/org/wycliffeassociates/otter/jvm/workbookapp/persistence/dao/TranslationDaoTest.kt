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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TranslationEntity
import java.io.File
import java.time.LocalDateTime

class TranslationDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-translation-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.translationDao }
    private val languageDao by lazy { database.languageDao }

    private val firstId = 1
    private val secondId = 2
    private val sampleTranslation = TranslationEntity(
        0, firstId, secondId, null, 1.0, 1.0
    )

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
        database.dsl.execute("PRAGMA foreign_keys = OFF;")
    }

    @After
    fun tearDown() {
        languageDao.fetchAll().forEach {
            languageDao.delete(it)
        }
        dao.fetchAll().forEach(dao::delete)
        database.close()
    }

    @Test
    fun testInsertTranslation() {
        assertEquals(0, dao.fetchAll().size)

        dao.insert(
            sampleTranslation.copy(sourceFk = firstId, targetFk = secondId)
        )

        assertEquals(
            "After inserting, the total number should increase by 1.",
            1,
            dao.fetchAll().size
        )

        dao.insert(
            sampleTranslation.copy(sourceFk = secondId, targetFk = secondId)
        )

        assertEquals(
            "After inserting, the total number should increase by 1.",
            2,
            dao.fetchAll().size
        )
    }

    @Test
    fun testFetchTranslations() {
        insertDefault()
        assertNotNull(
            dao.fetch(firstId, secondId)
        )
    }

    @Test
    fun testFetchNonExistingTranslation() {
        assertNull(dao.fetchById(999))
        assertNull(dao.fetch(999, 1000))
    }

    @Test
    fun testInsertThrowsException() {
        assertEquals(0, dao.fetchAll().size)

        try {
            val nonZeroId = 1
            dao.insert(
                sampleTranslation.copy(id = nonZeroId)
            )
            fail(
                "An exception is expected to throw when inserting a non-zero id field of translation entity."
            )
        } catch (e: InsertionException) {
            assertEquals("Entity ID is not 0", e.message)
        }

        assertEquals(
            "The total number should not change after the exception.",
            0,
            dao.fetchAll().size
        )
    }

    @Test
    fun testUpdateTranslation() {
        insertDefault()
        val original = dao.fetchAll().first()
        val updated = original.copy(
            sourceFk = firstId,
            targetFk = firstId,
            modifiedTs = LocalDateTime.now().toString()
        )

        dao.update(updated)
        val resultTranslation = dao.fetchById(original.id)

        assertNotEquals(original, resultTranslation)
        assertEquals(updated, resultTranslation)
    }

    @Test
    fun testDeleteTranslation() {
        insertDefault()
        assertEquals(1, dao.fetchAll().size)

        dao.fetchAll().forEach(dao::delete)

        assertEquals(0, dao.fetchAll().size)
    }

    private fun insertDefault() {
        dao.insert(sampleTranslation)
    }
}