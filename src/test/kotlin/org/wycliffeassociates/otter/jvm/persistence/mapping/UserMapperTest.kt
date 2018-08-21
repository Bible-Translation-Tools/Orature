package org.wycliffeassociates.otter.jvm.persistence.mapping

import data.model.User
import data.model.UserPreferences
import io.reactivex.Observable

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.wycliffeassociates.otter.jvm.persistence.JooqAssert
import org.wycliffeassociates.otter.jvm.persistence.data.LanguageStore
import org.wycliffeassociates.otter.jvm.persistence.repo.LanguageRepo
import org.wycliffeassociates.otter.jvm.persistence.repo.UserLanguageRepo
import jooq.tables.daos.UserPreferencesEntityDao
import jooq.tables.pojos.UserEntity
import jooq.tables.pojos.UserLanguagesEntity
import jooq.tables.pojos.UserPreferencesEntity
import org.wycliffeassociates.otter.jvm.persistence.mapping.UserMapper
import kotlin.math.exp

class UserMapperTest {
    private val mockUserLanguageRepo = Mockito.mock(UserLanguageRepo::class.java)
    private val mockLanguageDao = Mockito.mock(LanguageRepo::class.java)
    private val mockUserPreferencesDao = Mockito.mock(UserPreferencesEntityDao::class.java)

    val USER_DATA_TABLE = listOf(
        mapOf(
            "id" to "42",
            "audioHash" to "12345678",
            "audioPath" to "/my/really/long/path/name.wav",
            "imgPath" to "/my/really/long/path/name.png",
            "targetSlugs" to "ar",
            "sourceSlugs" to "en,cmn",
            "preferredSource" to "cmn",
            "preferredTarget" to "ar"
        ),
        mapOf(
            "id" to "10",
            "audioHash" to "abcdef",
            "audioPath" to "/my/path/name.wav",
            "imgPath" to "/my//path/name.png",
            "targetSlugs" to "es,fr",
            "sourceSlugs" to "en,es",
            "preferredSource" to "es",
            "preferredTarget" to "es"
        )
    )

    @Before
    fun setup() {
        BDDMockito.given(mockLanguageDao.getById(Mockito.anyInt())).will {
            Observable.just(LanguageStore.getById(it.getArgument(0)))
        }

    }

    @Test
    fun testIfUserEntityCorrectlyMappedToUser() {
        for (testCase in USER_DATA_TABLE) {
            // setup input
            val input = UserEntity(
                testCase["id"].orEmpty().toInt(),
                testCase["audioHash"],
                testCase["audioPath"],
                testCase["imgPath"]
            )


            // setup matching expected
            val expectedUserPreferences = UserPreferences(
                id = input.id,
                targetLanguage = LanguageStore.languages.filter { testCase["preferredTarget"] == it.slug }.first(),
                sourceLanguage = LanguageStore.languages.filter { testCase["preferredSource"] == it.slug }.first()
            )
            val expected = User(
                id = input.id,
                audioHash = input.audiohash,
                audioPath = input.audiopath,
                imagePath = input.imgpath,
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
                userPreferences = expectedUserPreferences
            )

            val allUserLanguageEntities = expected.sourceLanguages.map {
                UserLanguagesEntity(
                    expected.id,
                    it.id,
                    1
                )
            }.union(expected.targetLanguages.map {
                UserLanguagesEntity(
                    expected.id,
                    it.id,
                    0
                )
            }).toList()

            BDDMockito.given(mockUserLanguageRepo.getByUserId(input.id))
                .will { Observable.just(allUserLanguageEntities) }
            BDDMockito.given(mockUserPreferencesDao.fetchOneByUserfk(Mockito.anyInt())).will {
                UserPreferencesEntity(
                    input.id,
                    LanguageStore.languages.filter { testCase["preferredSource"] == it.slug }.first().id,
                    LanguageStore.languages.filter { testCase["preferredTarget"] == it.slug }.first().id

                )
            }

            val result = UserMapper(mockUserLanguageRepo, mockLanguageDao, mockUserPreferencesDao)
                .mapFromEntity(Observable.just(input))
                .blockingFirst()
            try {
                Assert.assertEquals(expected, result)
            } catch (e: AssertionError) {
                println("Input: $input")
                println("Result: $result")
                throw e
            }
        }
    }


    @Test
    fun testIfUserCorrectlyMappedToUserEntity() {
        for (testCase in USER_DATA_TABLE) {
            val input = User(
                id = testCase["id"].orEmpty().toInt(),
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
                    }
                    .toMutableList(),
                userPreferences = UserPreferences(
                    id = testCase["id"].orEmpty().toInt(),
                    targetLanguage = LanguageStore.languages
                        .filter {
                            testCase["preferredTarget"] == it.slug
                        }.first(),
                    sourceLanguage = LanguageStore.languages
                        .filter {
                            testCase["preferredSource"] == it.slug
                        }.first()
                )
            )

            val expected = UserEntity(
                input.id,
                input.audioHash,
                input.audioPath,
                input.imagePath
            )
            val result = UserMapper(mockUserLanguageRepo, mockLanguageDao, mockUserPreferencesDao)
                .mapToEntity(Observable.just(input))
                .blockingFirst()
            try {
                JooqAssert.assertUserEqual(expected, result)
            } catch (e: AssertionError) {
                println("Input: ${expected.audiohash}")
                println("Result: ${result.audiohash}")
                throw e
            }
        }
    }

}