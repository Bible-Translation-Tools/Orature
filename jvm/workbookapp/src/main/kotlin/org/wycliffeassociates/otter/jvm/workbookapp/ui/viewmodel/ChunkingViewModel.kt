/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.concurrent.TimeUnit
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javax.inject.Inject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.onChange
import tornadofx.runLater

const val ACTIVE = "chunking-wizard__step--active"
const val COMPLETE = "chunking-wizard__step--complete"
const val INACTIVE = "chunking-wizard__step--inactive"

const val SECONDS_ON_SCREEN = 10
private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class ChunkingViewModel: ViewModel() {

    val consumeStepColor = SimpleStringProperty(ACTIVE)
    val verbalizeStepColor = SimpleStringProperty(INACTIVE)
    val chunkStepColor = SimpleStringProperty(INACTIVE)

    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio = SimpleObjectProperty<AudioFile>()

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    init {
        titleProperty.onChange {
            when(it) {
                "Consume" -> {
                    consumeStepColor.set(ACTIVE)
                    verbalizeStepColor.set(INACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                "Verbalize" -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(ACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                "Chunking" -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(COMPLETE)
                    chunkStepColor.set(ACTIVE)
                }
            }
        }
    }


        private val width = Screen.getMainScreen().platformWidth
        private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)

        val waveformMinimapImage = SimpleObjectProperty<Image>()

        private val waveformSubject = PublishSubject.create<Image>()

        val waveform: Observable<Image>
            get() = waveformSubject

        var audioController: AudioPlayerController? = null
        val audioPlayer = SimpleObjectProperty<IAudioPlayer>()
        val isPlayingProperty = SimpleBooleanProperty(false)
        val compositeDisposable = CompositeDisposable()
        val positionProperty = SimpleDoubleProperty(0.0)
        var imageWidth: Double = 0.0

        val disposeables = mutableListOf<Disposable>()

        fun onDock(audio: AudioFile) {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            val audio = loadAudio(audio)
            createWaveformImages(audio)
            initializeAudioController()
        }

        fun loadAudio(audioFile: AudioFile): AudioFile {
            val player = audioConnectionFactory.getPlayer()
            val audio = AudioFile(audioFile.file)
            player.load(audioFile.file)
            audioPlayer.set(player)
            return audio
        }

        fun calculatePosition() {
            audioPlayer.get()?.let { audioPlayer ->
                val current = audioPlayer.getLocationInFrames()
                val duration = audioPlayer.getDurationInFrames().toDouble()
                val percentPlayed = current / duration
                val pos = percentPlayed * imageWidth
                positionProperty.set(pos)
            }
        }

        fun saveAndQuit() {
            compositeDisposable.clear()
        }

        fun initializeAudioController() {
            audioController = AudioPlayerController()
            audioController?.load(audioPlayer.get())
            isPlayingProperty.bind(audioController!!.isPlayingProperty)
        }

        fun pause() {
            audioController?.pause()
        }

        fun mediaToggle() {
            audioController?.toggle()
        }

        fun seek(location: Int) {
            audioController?.seek(location)
        }

        fun createWaveformImages(audio: AudioFile) {
            imageWidth = computeImageWidth(SECONDS_ON_SCREEN)

            val builder = WaveformImageBuilder(
                wavColor = Color.web(WAV_COLOR),
                background = Color.web(BACKGROUND_COLOR)
            )

            builder
                .build(
                    audio.reader(),
                    width = imageWidth.toInt(),
                    height = 50
                )
                .observeOnFx()
                .map { image ->
                    waveformMinimapImage.set(image)
                }
                .ignoreElement()
                .andThen(
                    builder.buildWaveformAsync(
                        audio.reader(),
                        width = imageWidth.toInt(),
                        height = height,
                        waveformSubject
                    )
                ).subscribe()
        }

        fun computeImageWidth(secondsOnScreen: Int): Double {
            val samplesPerScreenWidth = audioPlayer.get().getAudioReader()!!.sampleRate * secondsOnScreen
            val samplesPerPixel = samplesPerScreenWidth / width
            val pixelsInDuration = audioPlayer.get().getDurationInFrames() / samplesPerPixel
            return pixelsInDuration.toDouble()
        }

        fun getLocationInFrames(): Int {
            return audioPlayer.get().getLocationInFrames() ?: 0
        }

        fun getDurationInFrames(): Int {
            return audioPlayer.get().getDurationInFrames() ?: 0
        }
}
