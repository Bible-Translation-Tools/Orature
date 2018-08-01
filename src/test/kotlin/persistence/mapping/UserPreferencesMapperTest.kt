package persistence.mapping

import data.model.UserPreferences
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import persistence.JooqAssert
import persistence.data.LanguageStore
import persistence.repo.LanguageRepo
import jooq.tables.pojos.UserPreferencesEntity

class UserPreferencesMapperTest {
    val mockLanguageDao = Mockito.mock(LanguageRepo::class.java)

    @Before
    fun setup() {
        BDDMockito
            .given(mockLanguageDao.getById(Mockito.anyInt()))
            .will {
                Observable.just(LanguageStore.getById(it.getArgument(0)))
            }
    }

    @Test
    fun testIfUserPreferencesEntityCorrectlyMappedToUserPreferences() {
        val userPreferencesMapper = UserPreferencesMapper(mockLanguageDao)

        val inputEntity = UserPreferencesEntity(0, 2, 3)

        val expected = UserPreferences(
            id = 0,
            targetLanguage = LanguageStore.getById(3),
            sourceLanguage = LanguageStore.getById(2)
        )

        val result = userPreferencesMapper
            .mapFromEntity(Observable.just(inputEntity))
            .blockingFirst()

        Assert.assertEquals(expected, result)
    }

    @Test
    fun testIfUserPreferencesCorrectlyMappedToUserPreferencesEntity() {
        val userPreferencesMapper = UserPreferencesMapper(mockLanguageDao)

        val expectedEntity = UserPreferencesEntity(0, 2, 3)

        val input = UserPreferences(
            id = 0,
            targetLanguage = LanguageStore.getById(expectedEntity.targetlanguagefk),
            sourceLanguage = LanguageStore.getById(expectedEntity.sourcelanguagefk)
        )

        val result = userPreferencesMapper.mapToEntity(Observable.just(input)).blockingFirst()

        JooqAssert.assertUserPreferencesEqual(expected = expectedEntity, result = result)
    }
}