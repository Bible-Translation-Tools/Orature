package org.wycliffeassociates.otter.jvm.persistence.repo

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.persistence.JooqAssert
import org.wycliffeassociates.otter.jvm.persistence.data.LanguageStore
import org.wycliffeassociates.otter.jvm.persistence.mapping.LanguageMapper
import jooq.tables.pojos.UserLanguagesEntity
import jooq.tables.daos.UserEntityDao
import jooq.tables.pojos.UserEntity
import jooq.tables.pojos.UserPreferencesEntity
import org.wycliffeassociates.otter.jvm.persistence.repo.LanguageRepo
import org.wycliffeassociates.otter.jvm.persistence.repo.UserLanguageRepo
import java.io.File

class UserLanguageRepoTest {
    private lateinit var userLanguageRepo: UserLanguageRepo
    private var userId = 0
    private lateinit var inputUserLanguages: MutableList<UserLanguagesEntity>

    private val USER_LANGUAGES_TABLE = listOf(
        mapOf(
            "isSource" to 0,
            "languageId" to 1
        ),
        mapOf(
            "isSource" to 1,
            "languageId" to 1
        ),
        mapOf(
            "isSource" to 1,
            "languageId" to 2
        )
    )

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

        // setup languages
        val languageRepo = LanguageRepo(config, LanguageMapper())
        val userEntityDao = UserEntityDao(config)
        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()
        }

        val testUserEntity = UserEntity(
            null,
            "657175390964",
            "C:\\who\\uses\\windows\\anyway\\satya.png",
            "C:\\who\\uses\\windows\\anyway\\satya.wav"
        )
        userEntityDao.insert(testUserEntity)
        // insert test user entity
        userId = userEntityDao.fetchByAudiohash("657175390964").first().id
        userLanguageRepo = UserLanguageRepo(config)

        // setup test user languages
        inputUserLanguages = mutableListOf<UserLanguagesEntity>()
        for (testCase in USER_LANGUAGES_TABLE) {
            val userLanguageEntity = UserLanguagesEntity(
                userId,
                testCase["languageId"] ?: 0,
                testCase["isSource"]
            )
            inputUserLanguages.add(userLanguageEntity)
        }
    }

    @Test
    fun testInsertAndRetrieveByUser() {
        inputUserLanguages.forEach {
            userLanguageRepo.insert(it).blockingFirst()
        }

        val result = userLanguageRepo.getByUserId(userId).blockingFirst()

        for (i in 0 until result.size) {
            JooqAssert.assertUserLanguageEqual(expected = inputUserLanguages[i], result = result[i])
        }
    }

    @Test
    fun testInsertThrowsExceptionWithDuplicateEntry() {
        val userLanguageEntity = UserLanguagesEntity(
            userId,
            3,
            1

        )
        userLanguageRepo.insert(userLanguageEntity).blockingFirst()
        try {
            userLanguageRepo.insert(userLanguageEntity).blockingFirst()
            Assert.fail("Did not fail on second insert")
        } catch (e: Exception) {
            // everything passes because exception was thrown
        }
    }

    @Test
    fun testRetrieveAll() {
        inputUserLanguages.forEach {
            userLanguageRepo.insert(it).blockingFirst()
        }
        val result = userLanguageRepo.getAll().blockingFirst()
        for (i in 0 until result.size) {
            JooqAssert.assertUserLanguageEqual(expected = inputUserLanguages[i], result = result[i])
        }
    }

    @Test
    fun testDelete() {
        inputUserLanguages.forEach {
            userLanguageRepo.insert(it).blockingFirst()
        }

        val expected = inputUserLanguages.toMutableList() // use toMutableList to get copy of original list
        expected.remove(inputUserLanguages.first())

        userLanguageRepo.delete(inputUserLanguages.first()).blockingGet()

        val result = userLanguageRepo.getByUserId(userId).blockingFirst()

        for (i in 0 until result.size) {
            JooqAssert.assertUserLanguageEqual(expected = expected[i], result = result[i])
        }
    }

}