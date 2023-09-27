package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import tornadofx.*

class PeerEditViewModel : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val targetPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val positionProperty = SimpleDoubleProperty(0.0)
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()

    lateinit var waveform: Observable<Image>
    private var audioController: AudioPlayerController? = null
    var timer: AnimationTimer? = null

    fun dockPeerEdit() {
        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        startAnimationTimer()
    }

    fun undockPeerEdit() {
        sourcePlayerProperty.unbind()
        compositeDisposable.clear()
        stopAnimationTimer()
    }

    fun toggleAudio() {
        audioController?.toggle()
    }

    private fun startAnimationTimer() {
        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                calculatePosition()
            }
        }.apply { start() }
    }

    private fun stopAnimationTimer() {
        timer?.stop()
        timer = null
    }

    private fun calculatePosition() {
        sourcePlayerProperty.value?.let { audioPlayer ->
            val current = audioPlayer.getLocationInFrames()
            val duration = audioPlayer.getDurationInFrames().toDouble()
            val percentPlayed = current / duration
            val pos = percentPlayed * SECONDS_ON_SCREEN
            positionProperty.set(pos)
        }
    }
}