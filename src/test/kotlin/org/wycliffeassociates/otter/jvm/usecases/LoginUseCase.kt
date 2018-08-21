package org.wycliffeassociates.otter.jvm.usecases

import data.model.User
import data.persistence.AppPreferences
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import persistence.injection.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.persistence.repo.UserRepo

@RunWith(PowerMockRunner::class)
@PrepareForTest(DaggerPersistenceComponent::class, DaggerPersistenceComponent::class)
class LoginUseCaseTest {

    private val mockPersistenceBuilder = Mockito.mock(
            DaggerPersistenceComponent.Builder::class.java,
            Mockito.RETURNS_DEEP_STUBS
    )
    private val mockUserDao = Mockito.mock(UserRepo::class.java)
    private val mockAppPreferences = Mockito.mock(AppPreferences::class.java)

    @Before
    fun setup(){
        PowerMockito.mockStatic(DaggerPersistenceComponent::class.java)

        Mockito
                .`when`(DaggerPersistenceComponent.builder())
                .thenReturn(mockPersistenceBuilder)
        Mockito
                .`when`(mockPersistenceBuilder
                        .build()
                        .injectDatabase()
                        .getUserDao()
                ).thenReturn(mockUserDao)

        Mockito
                .`when`(DaggerPersistenceComponent.builder())
                .thenReturn(mockPersistenceBuilder)
        Mockito
                .`when`(mockPersistenceBuilder
                        .build()
                        .injectPreferences()
                ).thenReturn(mockAppPreferences)

    }

    @Test
    fun testLoginUseCaseReturnsMostCurrentUserIfExists() {
        val mockUser = Mockito.mock(User::class.java)
        val useCase = LoginUseCase()
        Mockito
                .`when`(mockAppPreferences.getCurrentUserId())
                .thenReturn(1)
        Mockito
                .`when`(mockUserDao.getById(1))
                .thenReturn(Observable.just(mockUser))

        val result = useCase.getCurrentUser().blockingFirst()
        Assert.assertEquals(mockUser, result)
    }

    @Test
    fun testLoginUseCaseReturnsEmptyUserIfNoneExists() {
        val useCase = LoginUseCase()
        Mockito
                .`when`(mockAppPreferences.getCurrentUserId())
                .thenReturn(null)

        val result = useCase.getCurrentUser()
        Assert.assertTrue(result.isEmpty.blockingGet())
    }
}