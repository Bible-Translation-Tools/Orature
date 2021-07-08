package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioDevicesRepository
import javax.inject.Inject
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

internal val SAMPLE_RATE = 44100F // Hz
internal val SAMPLE_SIZE = 16 // bits
internal val CHANNELS = 1
internal val SIGNED = true
internal val BIG_ENDIAN = false

class AudioDevicesRepository @Inject constructor(
    private val preferences: IAppPreferences
) : IAudioDevicesRepository {

    private val logger = LoggerFactory.getLogger(AudioDevicesRepository::class.java)

    private val audioFormat = AudioFormat(
        SAMPLE_RATE,
        SAMPLE_SIZE,
        CHANNELS,
        SIGNED,
        BIG_ENDIAN
    )

    override fun getPlayers(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val playerInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
                val playerLines = mixer.getSourceLineInfo(playerInfo)
                playerLines.isNotEmpty()
            }.toList()
        }
    }

    override fun getRecorders(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val recorderInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
                val recorderLines = mixer.getTargetLineInfo(recorderInfo)
                recorderLines.isNotEmpty()
            }.toList()
        }
    }

    override fun getCurrentPlayer(): Single<Mixer.Info> {
        return preferences.audioPlaybackDevice()
            .flatMap { deviceName ->
                getPlayers().map {
                    it.singleOrNull { it.name == deviceName } ?: it.first()
                }
            }
    }

    override fun getCurrentRecorder(): Single<Mixer.Info> {
        return preferences.audioRecordDevice()
            .flatMap { deviceName ->
                getRecorders().map {
                    it.singleOrNull { it.name == deviceName } ?: it.first()
                }
            }
    }

    override fun setPlayer(mixer: Mixer.Info): Completable {
        return preferences.setAudioPlaybackDevice(mixer.name)
    }

    override fun setRecorder(mixer: Mixer.Info): Completable {
        return preferences.setAudioRecordDevice(mixer.name)
    }
}
