package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import javax.sound.sampled.Mixer

interface IAudioDevicesRepository {
    fun getPlayers(): Single<List<Mixer.Info>>
    fun getRecorders(): Single<List<Mixer.Info>>
    fun getCurrentPlayer(): Single<Mixer.Info>
    fun getCurrentRecorder(): Single<Mixer.Info>
    fun setPlayer(mixer: Mixer.Info): Completable
    fun setRecorder(mixer: Mixer.Info): Completable
}
