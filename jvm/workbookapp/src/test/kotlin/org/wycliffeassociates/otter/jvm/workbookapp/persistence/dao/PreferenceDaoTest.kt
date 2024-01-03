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

import jooq.Tables.PREFERENCES
import org.jooq.exception.DataAccessException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.PreferenceEntity
import java.io.File

class PreferenceDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-preference-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.preferenceDao }

    private val prefs = listOf(
        PreferenceEntity("k1", "v1"),
        PreferenceEntity("k2", "v2")
    )

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
        prefs.forEach(dao::insert)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testFetchByKey() {
        val p = prefs[0]
        val result = dao.fetchByKey(p.key)
        Assert.assertEquals(p.value, result?.value)
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(2, count())

        val p = PreferenceEntity("new-preference", "new-value")
        dao.insert(p)

        Assert.assertEquals(
            "After inserting, the total number should increase by 1.",
            3,
            count()
        )
        Assert.assertEquals(p, dao.fetchByKey(p.key))

        try {
            dao.insert(p)
            Assert.fail("An exception is expected to throw when inserting a duplicated preference")
        } catch(e: DataAccessException) { }

        Assert.assertEquals(
            "The total number of objects should not change after the insertion exception.",
            3,
            count()
        )
    }

    @Test
    fun testUpdate() {
        val p = prefs[0]
        val updated = p.copy(value = "updated-value")
        dao.update(updated)

        Assert.assertEquals(updated, dao.fetchByKey(p.key))
    }

    @Test
    fun testUpsert() {
        val pref = PreferenceEntity("k3", "v3")
        Assert.assertNull(dao.fetchByKey(pref.key))

        // insert
        dao.upsert(pref)

        var result = dao.fetchByKey(pref.key)
        Assert.assertNotNull(result)
        Assert.assertEquals(pref, result)
        Assert.assertEquals(
            "The total number should increase by 1.",
            3,
            count()
        )

        // update
        val newValue = "v3-updated"
        dao.upsert(pref.copy(value = newValue))

        result = dao.fetchByKey(pref.key)
        Assert.assertNotNull(result)
        Assert.assertEquals(newValue, result!!.value)
        Assert.assertEquals(
            "The total number should not change after updating.",
            3,
            count()
        )
    }

    @Test
    fun testDelete() {
        val entity = dao.fetchByKey(prefs[0].key)
        Assert.assertNotNull(entity)
        Assert.assertEquals(prefs.size, count())

        dao.delete(entity!!)

        Assert.assertNull(dao.fetchByKey(entity.key))
        Assert.assertEquals(prefs.size - 1, count())
    }

    /**
     * Returns the total number of records for PREFERENCES table,
     * since PreferenceDao does not contain such method nor fetchAll().
     */
    private fun count(): Int {
        return database.dsl
            .selectCount()
            .from(PREFERENCES)
            .fetchOne {
                it.value1()
            }!!
    }
}