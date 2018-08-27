package org.wycliffeassociates.otter.jvm

import org.wycliffeassociates.otter.common.data.model.User
import io.reactivex.Observable
import org.wycliffeassociates.otter.jvm.device.audio.injection.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent
import java.io.File

class LoginUseCase {
    // dagger injects userDao
    private val userDao = DaggerPersistenceComponent
            .builder()
            .build()
            .injectDatabase()
            .getUserDao()

    private val appPreferences = DaggerPersistenceComponent
            .builder()
            .build()
            .injectPreferences()

    private val audioPlayer = DaggerAudioComponent
            .builder()
            .build()
            .injectPlayer()

    fun getUsers(): Observable<List<User>> {
        return userDao.getAll()
    }

    fun playUserAudio(user: User) {
        val audioFile = File(user.audioPath)
        audioPlayer.load(audioFile)
        audioPlayer.play()
    }

    fun getCurrentUser(): Observable<User> {
        val userId = appPreferences.getCurrentUserId()
        return if (userId != null)
            userDao.getById(userId)
        else
            Observable.empty<User>()
    }

    fun setCurrentUser(user: User) {
        appPreferences.setCurrentUserId(user.id)
    }
}