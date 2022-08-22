package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.jooq.exception.DataAccessException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.LanguageEntity
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
        dao.insertAll(
            languages.map { LanguageMapper().mapToEntity(it) }
        )
    }

    @After
    fun tearDown() {
        dao.fetchAll().forEach(dao::delete)
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
        Assert.assertEquals(1, gwLanguages.size)
        Assert.assertTrue(gwLanguages.all { it.gateway == 1 })
    }

    @Test
    fun testFetchTargets() {
        val targetLanguages = dao.fetchTargets()
        Assert.assertEquals(2, targetLanguages.size)
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
        val aa = languages.find { it.slug == "aa" }!!
        val entity = dao.fetchBySlug("en")!!

        val duplicated = entity.copy(
            slug = aa.slug
        )

        try {
            dao.update(duplicated)
            Assert.fail("An exception is expected to throw when setting a duplicated language slug. ")
        } catch (e: DataAccessException) { }

    }
}