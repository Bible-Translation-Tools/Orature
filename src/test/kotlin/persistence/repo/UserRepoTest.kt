package persistence.repo

import data.model.User
import data.model.UserPreferences
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.*
import org.sqlite.SQLiteDataSource
import persistence.data.LanguageStore
import persistence.mapping.LanguageMapper
import persistence.mapping.UserMapper
import persistence.mapping.UserPreferencesMapper
import jooq.tables.daos.UserEntityDao
import jooq.tables.daos.UserPreferencesEntityDao
import org.jooq.DSLContext
import org.junit.runners.MethodSorters
import java.io.File

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
class UserRepoTest {
    private lateinit var config: Configuration
    private lateinit var userRepo: UserRepo
    private lateinit var languageRepo: LanguageRepo
    private lateinit var users: MutableList<User>

    val USER_DATA_TABLE = listOf(
        mapOf(
            "audioHash" to "12345678",
            "audioPath" to "/my/really/long/path/name.wav",
            "imgPath" to "/my/really/long/path/name.png",
            "targetSlugs" to "ar,gln",
            "sourceSlugs" to "en,cmn",
            "newTargets" to "es",
            "newSources" to "fr",
            "removeTargets" to "ar",
            "removeSources" to "en",
            "newPrefSource" to "cmn",
            "newPrefTarget" to "gln"
        )
    )

    @Before
    fun setup() {
        Class.forName("org.sqlite.JDBC")
        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:test.sqlite"
        dataSource.config.toProperties().setProperty("foreign_keys", "true")
        config = DSL.using(dataSource, SQLDialect.SQLITE).configuration()
        val file = File("src/main/Resources/TestAppDbInit.sql")
        // running sql schema to drop and create tables in database
        var sql = StringBuffer()
        file.forEachLine {
            sql.append(it)
            if (it.contains(";")) {
                config.dsl().fetch(sql.toString())
                sql.delete(0, sql.length)
            }
        }

        languageRepo = LanguageRepo(config, LanguageMapper())
        userRepo = UserRepo(
            config,
            UserMapper(UserLanguageRepo(config), languageRepo, UserPreferencesEntityDao(config)),
            UserPreferencesMapper(languageRepo)
        )
        LanguageStore.languages.forEach {
            it.id = languageRepo.insert(it).blockingFirst()
        }
        val userPreference = UserPreferences(
            id = 0,
            targetLanguage = LanguageStore.getLanguageForSlug("ar"),
            sourceLanguage = LanguageStore.getLanguageForSlug("en")
        )
        users = ArrayList()
        USER_DATA_TABLE.forEach { testCase ->
            users.add(
                User(
                    audioHash = testCase["audioHash"].orEmpty(),
                    audioPath = testCase["audioPath"].orEmpty(),
                    imagePath = testCase["imgPath"].orEmpty(),
                    targetLanguages = LanguageStore.languages
                        .filter {
                            testCase["targetSlugs"]
                                .orEmpty()
                                .split(",")
                                .contains(it.slug)
                        }.toMutableList(),
                    sourceLanguages = LanguageStore.languages
                        .filter {
                            testCase["sourceSlugs"]
                                .orEmpty()
                                .split(",")
                                .contains(it.slug)
                        }.toMutableList(),
                    userPreferences = userPreference
                )
            )
        }
    }

    @Test
    fun insertAndRetrieveTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            user.userPreferences.id = user.id
            val result = userRepo.getById(user.id).blockingFirst()
            Assert.assertEquals(user, result)
        }
    }

    @Test
    fun insertThrowsExceptionFromDuplicateEntry() {
        users.forEach {
            userRepo.insert(it).blockingFirst()
            try {
                userRepo.insert(it).blockingFirst()
                Assert.fail()
            } catch (e: Exception) {
                // success
            }
        }
    }

    @Test
    fun retrieveAllTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            user.userPreferences.id = user.id
        }
        Assert.assertEquals(users, userRepo.getAll().blockingFirst())
    }

    @Test
    fun addLanguagesTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            // grab from the db since we need user preferences to have the correct assigned id
            val updatedUser = userRepo.getById(user.id).blockingFirst()

            // get the new source and target slugs from the test case table
            val newSourceSlugs = USER_DATA_TABLE.filter { it["audioHash"].orEmpty() == user.audioHash }
                .first()["newSources"].orEmpty().split(",")
            val newTargetSlugs = USER_DATA_TABLE.filter { it["audioHash"].orEmpty() == user.audioHash }
                .first()["newTargets"].orEmpty().split(",")

            // add the new languages from the store
            val newSources = LanguageStore.languages.filter { newSourceSlugs.contains(it.slug) }
            val newTargets = LanguageStore.languages.filter { newTargetSlugs.contains(it.slug) }

            updatedUser.sourceLanguages.addAll(newSources)
            updatedUser.targetLanguages.addAll(newTargets)

            userRepo.update(updatedUser).blockingAwait()

            // check the result
            val result = userRepo.getById(updatedUser.id).blockingFirst()
            Assert.assertTrue(result.sourceLanguages.containsAll(newSources))
            Assert.assertTrue(result.targetLanguages.containsAll(newTargets))
        }

    }


    @Test
    fun removedLanguagesTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            // grab from the db since we need user preferences to have the correct assigned id
            val updatedUser = userRepo.getById(user.id).blockingFirst()
            // get the new source and target slugs from the test case table
            val removeSourcesSlugs = USER_DATA_TABLE.filter { it["audioHash"].orEmpty() == user.audioHash }
                .first()["removeSources"].orEmpty().split(",")
            val removeTargetsSlugs = USER_DATA_TABLE.filter { it["audioHash"].orEmpty() == user.audioHash }
                .first()["removeTargets"].orEmpty().split(",")

            val removeSources = LanguageStore.languages.filter { removeSourcesSlugs.contains(it.slug) }
            val removeTargets = LanguageStore.languages.filter { removeTargetsSlugs.contains(it.slug) }

            updatedUser.sourceLanguages.removeAll(removeSources)
            updatedUser.targetLanguages.removeAll(removeTargets)

            userRepo.update(updatedUser).blockingAwait()

            // check the result
            val result = userRepo.getById(updatedUser.id).blockingFirst()
            Assert.assertTrue(result.sourceLanguages.all { !removeSources.contains(it) })
            Assert.assertTrue(result.targetLanguages.all { !removeTargets.contains(it) })
        }

    }


    @Test
    fun setSourceLanguageTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            user.userPreferences.id = user.id
            val newSourceSlug = USER_DATA_TABLE.filter { it["audioHash"] == user.audioHash }
                .first()["newPrefSource"] ?: ""
            val newSource = LanguageStore.getLanguageForSlug(newSourceSlug)
            user.userPreferences.sourceLanguage = newSource
            userRepo.update(user).blockingAwait()

            val result = userRepo.getById(user.id).blockingFirst()
            Assert.assertEquals(newSource, result.userPreferences.sourceLanguage)
        }
    }

    @Test
    fun setTargetLanguageTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            user.userPreferences.id = user.id
            val newTargetSlug = USER_DATA_TABLE.filter { it["audioHash"] == user.audioHash }
                .first()["newPrefTarget"] ?: ""
            val newTarget = LanguageStore.getLanguageForSlug(newTargetSlug)
            user.userPreferences.targetLanguage = newTarget
            userRepo.update(user).blockingAwait()

            val result = userRepo.getById(user.id).blockingFirst()
            Assert.assertEquals(newTarget, result.userPreferences.targetLanguage)
        }
    }

    @Test
    fun deleteTest() {
        users.forEach { user ->
            user.id = userRepo.insert(user).blockingFirst()
            userRepo.delete(user).blockingAwait()
            val result = UserEntityDao(config).findAll()
            Assert.assertTrue(result.toList().isEmpty())
        }

    }

    @After
    fun tearDown() {
        config.dsl().close()
    }
}