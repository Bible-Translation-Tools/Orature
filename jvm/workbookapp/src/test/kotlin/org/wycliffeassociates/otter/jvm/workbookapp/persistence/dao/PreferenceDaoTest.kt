package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.jooq.exception.DataAccessException
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
    private val database = AppDatabase(testDatabaseFile)
    private val dao = database.preferenceDao

    private val prefs = listOf(
        PreferenceEntity("k1", "v1"),
        PreferenceEntity("k2", "v2")
    )

    @Before
    fun setup() {
        prefs.forEach(dao::insert)
    }

    @Test
    fun testFetchByKey() {
        val p = prefs[0]
        val result = dao.fetchByKey(p.key)
        Assert.assertEquals(p.value, result.value)
    }

    @Test
    fun testInsert() {
        val p = PreferenceEntity("new-preference", "new-value")
        dao.insert(p)
        Assert.assertEquals(p, dao.fetchByKey(p.key))

        try {
            dao.insert(p)
            Assert.fail("An exception is expected to throw when inserting a duplicated preference")
        } catch(e: DataAccessException) { }
    }

    @Test
    fun testUpdate() {
        val p = prefs[0]
        val updated = p.copy(value = "updated-value")
        dao.update(updated)

        Assert.assertEquals(updated, dao.fetchByKey(p.key))
    }
}