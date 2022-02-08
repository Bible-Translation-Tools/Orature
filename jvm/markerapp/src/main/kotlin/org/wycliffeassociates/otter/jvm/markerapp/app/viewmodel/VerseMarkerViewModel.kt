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
package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import java.lang.Integer.min
import java.util.concurrent.TimeUnit
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.MarkerPlacementWaveformSkin
import org.wycliffeassociates.otter.jvm.markerapp.app.view.ScrollingWaveformSkin

const val SECONDS_ON_SCREEN = 10
private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class VerseMarkerViewModel : ViewModel() {
    private val width = Screen.getMainScreen().platformWidth
    private val height = min(Screen.getMainScreen().platformHeight, 500)

    val waveformMinimapImage = SimpleObjectProperty<Image>()

    lateinit var waveformAsyncBuilder: Completable
    lateinit var waveform: Observable<Image>

    val logger = LoggerFactory.getLogger(VerseMarkerViewModel::class.java)

    var markerStateProperty = SimpleObjectProperty<VerseMarkerModel>()
    val markers by markerStateProperty

    var audioController: AudioPlayerController? = null

    val audioPlayer = SimpleObjectProperty<IAudioPlayer>()

    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val compositeDisposable = CompositeDisposable()
    val positionProperty = SimpleDoubleProperty(0.0)
    var imageWidth: Double = 0.0

    val disposeables = mutableListOf<Disposable>()

    private var audioFile: File? = null

    fun onDock() {
        val audio = loadAudio()
        loadMarkers(audio)
        loadTitles()

        createWaveformImages(audio)
    }

    fun loadAudio(): AudioFile {
        val scope = scope as ParameterizedScope
        val player = (scope.workspace.params["audioConnectionFactory"] as AudioConnectionFactory).getPlayer()
        val audioFile = File(scope.parameters.named["wav"])
        val audio = AudioFile(audioFile)
        player.load(audioFile)
        audioPlayer.set(player)
        return audio
    }

    fun loadMarkers(audio: AudioFile) {
        val initialMarkerCount = audio.metadata.getCues().size
        scope as ParameterizedScope
        val totalMarkers: Int = scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        val markers = VerseMarkerModel(audio, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("$it/$totalMarkers")
        }
        markerStateProperty.set(markers)
    }

    fun loadTitles() {
        scope as ParameterizedScope
        headerTitle.set(scope.parameters.named["action_title"])
        headerSubtitle.set(scope.parameters.named["content_title"])
    }

    fun writeMarkers(): Completable {
        audioPlayer.get()?.pause()
        audioPlayer.get()?.close()
        return markerStateProperty.get()?.writeMarkers() ?: Completable.complete()
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
        waveformMinimapImage.set(null)

        (scope as ParameterizedScope).let {
            writeMarkers()
                .doOnError { e ->
                    logger.error("Error in closing the maker app", e)
                }
                .delay(300, TimeUnit.MILLISECONDS) // exec after UI clean up
                .subscribe {
                    runLater {
                        it.navigateBack()
                        System.gc()
                    }
                }
        }
    }

    fun placeMarker() {
        markerStateProperty.get()?.addMarker(audioPlayer.get().getLocationInFrames())
    }

    fun seekNext() {
        val wasPlaying = audioPlayer.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerStateProperty.get()?.let { markers ->
            seek(markers.seekNext(audioPlayer.get().getLocationInFrames()))
        }
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun seekPrevious() {
        val wasPlaying = audioPlayer.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerStateProperty.get()?.let { markers ->
            seek(markers.seekPrevious(audioPlayer.get().getLocationInFrames()))
        }
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(slider)
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

        WaveformImageBuilder(
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        ).apply {
            build(
                audio.reader(),
                width = imageWidth.toInt(),
                height = 50
            )
                .observeOnFx()
                .subscribe { image ->
                    println("adding image from builder ${image.width}x${image.height}")
                    waveformMinimapImage.set(image)
                }.also {
                    compositeDisposable.add(it)
                }

            val waveformSubject = PublishSubject.create<Image>()
            waveform = waveformSubject
            waveformAsyncBuilder = buildWaveformAsync(
                audio.reader(),
                width = imageWidth.toInt(),
                height = height,
                waveformSubject
            )
        }
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

open class ScrollingWaveform() : Control() {

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    val positionProperty = SimpleDoubleProperty(0.0)

    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}

    fun addWaveformImage(image: Image) {
        println("adding image")
        (skin as ScrollingWaveformSkin).addWaveformImage(image)
    }

    fun freeImages() {
        (skin as ScrollingWaveformSkin).freeImages()
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScrollingWaveformSkin(this)
    }
}

class MarkerPlacementWaveform(
    val topNode: Node
) : ScrollingWaveform() {

    val markerStateProperty = SimpleObjectProperty<VerseMarkerModel>()

    var onSeekNext: () -> Unit = {}
    var onSeekPrevious: () -> Unit = {}
    var onPlaceMarker: () -> Unit = {}
    var topTrack: Node? = topNode
    var bottomTrack: Node? = null

    init {
        markerStateProperty.get()?.let { markers ->
            (skin as MarkerPlacementWaveformSkin).addHighlights(markers.highlightState)
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerPlacementWaveformSkin(this)
    }
}
