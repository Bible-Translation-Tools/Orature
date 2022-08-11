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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TranslationEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File
import java.time.LocalDateTime

class TranslationDaoTest {
    private val testDatabaseFile = File.createTempFile("test-translation-dao", ".sqlite").also(File::deleteOnExit)
    private val database = AppDatabase(testDatabaseFile)
    private val dao = database.translationDao
    private val languageDao = database.languageDao

    private val languages = listOf(
        Language("en", "English", "English", "ltr", true, "US", 1),
        Language("en-test", "English-test", "English-test", "ltr", true, "US", 2)
    )

    @Before
    fun setup() {
        languages.map {
            LanguageMapper().mapToEntity(it)
        }.also {
            languageDao.insertAll(it)
        }

        dao.fetchAll().forEach(dao::delete)
    }

    @After
    fun tearDown() {
        languageDao.fetchAll().forEach {
            languageDao.delete(it)
        }
    }

    @Test
    fun testInsertTranslation() {
        val firstLanguage = languages[0]
        val secondLanguage = languages[1]
        var result = 0

        result = dao.insert(
            TranslationEntity(0, firstLanguage.id, secondLanguage.id, null, 1.0, 1.0)
        )
        assertEquals(1, result)

        result = dao.insert(
            TranslationEntity(0, secondLanguage.id, firstLanguage.id, null, 1.0, 1.0)
        )
        assertEquals(2, result)
    }

    @Test
    fun testFetchTranslations() {
        insertDefault()
        assertNotNull(
            dao.fetch(languages[0].id, languages[1].id)
        )
        assertNotNull(dao.fetchById(1))
        assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testUpdateTranslation() {
        insertDefault()
        val original = dao.fetchAll().first()
        val updated = original.copy(
            sourceFk = languages[0].id,
            targetFk = languages[0].id,
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
        dao.insert(
            TranslationEntity(
                0, languages[0].id, languages[1].id, null, 1.0, 1.0
            )
        )
    }
}