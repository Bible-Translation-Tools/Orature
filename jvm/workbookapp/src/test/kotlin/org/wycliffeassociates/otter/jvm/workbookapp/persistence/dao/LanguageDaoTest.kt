package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File

class LanguageDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-language-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private val database = AppDatabase(testDatabaseFile)
    private val dao = database.languageDao

    private val languages = listOf(
        Language("aa", "Afar", "Afaraf", "ltr", false, "Africa"),
        Language("en", "English", "English", "ltr", true, "Europe"),
        Language("ade", "Adele", "Adele", "ltr", false, "Africa")
    )

    @Before
    fun setup() {

    }

    @After
    fun tearDown() {
        dao.fetchAll().forEach(dao::delete)
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(0, dao.fetchAll().size)
        dao.insert(
            LanguageMapper().mapToEntity(languages.first())
        )
        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testInsertAll() {
        Assert.assertEquals(0, dao.fetchAll().size)
        insertLanguages()
        Assert.assertEquals(languages.size, dao.fetchAll().size)
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
    }

    @Test
    fun testFetchGateway() {
        insertLanguages()

        val gwLanguages = dao.fetchGateway()
        Assert.assertEquals(1, gwLanguages.size)
        Assert.assertTrue(gwLanguages.all { it.gateway == 1 })
    }

    @Test
    fun testFetchTargets() {
        insertLanguages()

        val targetLanguages = dao.fetchTargets()
        Assert.assertEquals(2, targetLanguages.size)
        Assert.assertTrue(targetLanguages.all { it.gateway == 0 })
    }

    @Test
    fun testFetchBySlug() {
        insertLanguages()

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
        insertLanguages()

        Assert.assertNotNull(dao.fetchById(1))
        Assert.assertNull(dao.fetchById(999))
    }

    private fun insertLanguages() {
        dao.insertAll(
            languages.map { LanguageMapper().mapToEntity(it) }
        )
    }
}