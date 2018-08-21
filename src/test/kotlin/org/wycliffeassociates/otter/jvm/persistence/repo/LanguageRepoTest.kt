package org.wycliffeassociates.otter.jvm.persistence.repo

import org.wycliffeassociates.otter.common.data.model.Language
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.persistence.data.LanguageStore
import org.wycliffeassociates.otter.jvm.persistence.mapping.LanguageMapper
import jooq.tables.daos.UserEntityDao
import jooq.tables.daos.UserLanguagesEntityDao
import jooq.tables.pojos.*
import org.wycliffeassociates.otter.jvm.persistence.repo.LanguageRepo
import java.io.File

class LanguageRepoTest {
    private lateinit var languageRepo: LanguageRepo
    private lateinit var userEntityDao: UserEntityDao
    private lateinit var userLanguageDao: UserLanguagesEntityDao
    private val mockLanguageMapper = Mockito.mock(LanguageMapper::class.java)

    @Before
    fun setup() {
        Class.forName("org.sqlite.JDBC")
        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:test.sqlite"
        dataSource.config.toProperties().setProperty("foreign_keys", "true")

        val config = DSL.using(dataSource, SQLDialect.SQLITE).configuration()
        val file = File("src/main/Resources/TestAppDbInit.sql")
        var sql = StringBuffer()
        file.forEachLine {
            sql.append(it)
            if (it.contains(";")) {
                config.dsl().fetch(sql.toString())
                sql.delete(0, sql.length)
            }
        }

        userLanguageDao = UserLanguagesEntityDao(config)
        userEntityDao = UserEntityDao(config)
        languageRepo = LanguageRepo(config, LanguageMapper())
    }

    @Test
    fun insertAndRetrieveByIdTest() {
        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()
            Assert.assertEquals(it, languageRepo.getById(it.id).blockingFirst())
        }
    }

    @Test
    fun retrieveAllTest() {
        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()
        }
        Assert.assertEquals(LanguageStore.languages, languageRepo.getAll().blockingFirst())
    }

    @Test
    fun retrieveGatewayLanguagesTest() {
        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()
        }
        Assert.assertEquals(
            languageRepo
                .getGatewayLanguages()
                .blockingFirst(),
            LanguageStore.languages.filter {
                it.isGateway
            }
        )
    }

    @Test
    fun updateTest() {
        LanguageStore.languages.forEach {
            // insert the original language
            it.id = languageRepo.insert(it).blockingFirst()

            // create the updated version of the language
            val updatedLanguage = Language(
                name = "Khoisan",
                anglicizedName = "Khoisan",
                isGateway = false,
                slug = "khi"
            )
            updatedLanguage.id = it.id

            // try to update the language in the repo
            languageRepo.update(updatedLanguage).blockingGet()

            Assert.assertEquals(languageRepo.getById(updatedLanguage.id).blockingFirst(), updatedLanguage)

            // roll back the tests for the next case
            languageRepo.update(it).blockingGet()
        }
    }

    @Test
    fun deleteTest() {
        val testUser = UserEntity(
            null,
            "12345678",
            "somepath",
            "betterPath"
        )
        userEntityDao.insert(testUser)
        testUser.id = userEntityDao.fetchByAudiohash("12345678").first().id

        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()

            val testUserLanguage = UserLanguagesEntity(
                testUser.id,
                it.id,
                0
            )

            userLanguageDao.insert(testUserLanguage)

            languageRepo.delete(it).blockingGet()
            try {
                Assert.assertTrue(
                    userLanguageDao
                        .fetchByUserfk(testUser.id)
                        .isEmpty()
                )
            } catch (e: AssertionError) {
                println("Failed on")
                println(it.slug)
                throw e
            }
        }
    }
}