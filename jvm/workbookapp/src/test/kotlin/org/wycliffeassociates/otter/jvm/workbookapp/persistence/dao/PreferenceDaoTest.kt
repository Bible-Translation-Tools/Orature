package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

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
//        database.close()
    }

    @Test
    fun testFetchByKey() {
        val p = prefs[0]
        val result = dao.fetchByKey(p.key)
        Assert.assertEquals(p.value, result?.value)
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

    @Test
    fun testUpsert() {
        val pref = PreferenceEntity("k3", "v3")
        Assert.assertNull(dao.fetchByKey(pref.key))

        // insert
        dao.upsert(pref)

        var result = dao.fetchByKey(pref.key)
        Assert.assertNotNull(result)
        Assert.assertEquals(pref, result)

        // update
        val newValue = "v3-updated"
        dao.upsert(pref.copy(value = newValue))

        result = dao.fetchByKey(pref.key)
        Assert.assertNotNull(result)
        Assert.assertEquals(newValue, result!!.value)
    }

    @Test
    fun testDelete() {
        val entity = dao.fetchByKey(prefs[0].key)
        Assert.assertNotNull(entity)

        dao.delete(entity!!)

        Assert.assertNull(dao.fetchByKey(entity.key))
    }
}