package org.wycliffeassociates.otter.common.device

import io.reactivex.Maybe
import io.reactivex.Single
import javax.sound.sampled.Mixer

interface IAudioDevice {
    fun getOutputDevices(): Single<List<Mixer.Info>>
    fun getInputDevices(): Single<List<Mixer.Info>>
    fun getOutputDevice(name: String?): Maybe<Mixer.Info>
    fun getInputDevice(name: String?): Maybe<Mixer.Info>
}
