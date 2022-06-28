package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.binding.IntegerBinding
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel

interface IMarkerViewModel {
    var markerModel: VerseMarkerModel?
    val markers: ObservableList<ChunkMarkerModel>
    val markerCountProperty: IntegerBinding
    val audioPlayer: SimpleObjectProperty<IAudioPlayer>
    var audioController: AudioPlayerController?
    val currentMarkerNumberProperty: SimpleIntegerProperty
    val positionProperty: SimpleDoubleProperty
    var imageWidthProperty: SimpleDoubleProperty

    var resumeAfterScroll: Boolean

    fun placeMarker() {
        markerModel?.let { markerModel ->
            markerModel.addMarker(audioPlayer.get().getLocationInFrames())
            markers.setAll(markerModel.markers)
        }
    }

    fun seekNext() {
        val wasPlaying = audioPlayer.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerModel?.let { markerModel ->
            seek(markerModel.seekNext(audioPlayer.get().getLocationInFrames()))
        } ?: run { seek(audioPlayer.get().getLocationInFrames()) }
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun seekPrevious() {
        val wasPlaying = audioPlayer.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerModel?.let { markerModel ->
            seek(markerModel.seekPrevious(audioPlayer.get().getLocationInFrames()))
        } ?: run { seek(audioPlayer.get().getLocationInFrames()) }
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun seek(location: Int) {
        audioController?.seek(location)
        updateCurrentPlaybackMarker(location)
    }


    private fun updateCurrentPlaybackMarker(currentFrame: Int) {
        markerModel?.let { markerModel ->
            val currentMarkerFrame = markerModel.seekCurrent(currentFrame)
            val currentMarker = markers.find { it.frame == currentMarkerFrame }
            val index = currentMarker?.let { markers.indexOf(it) } ?: 0
            currentMarkerNumberProperty.set(index)
        }
    }

    fun requestAudioLocation(): Int {
        return audioPlayer.value?.getLocationInFrames() ?: 0
    }

    fun undoMarker() {
        markerModel?.let { markerModel ->
            markerModel.undo()
            markers.setAll(markerModel.markers)
        }
    }

    fun redoMarker() {
        markerModel?.let { markerModel ->
            markerModel.redo()
            markers.setAll(markerModel.markers)
        }
    }

    fun calculatePosition() {
        audioPlayer.get()?.let { audioPlayer ->
            val current = audioPlayer.getLocationInFrames()
            val duration = audioPlayer.getDurationInFrames().toDouble()
            val percentPlayed = current / duration
            val pos = percentPlayed * imageWidthProperty.value
            positionProperty.set(pos)
            updateCurrentPlaybackMarker(current)
        }
    }

    fun getLocationInFrames(): Int {
        return audioPlayer.get().getLocationInFrames() ?: 0
    }

    fun getDurationInFrames(): Int {
        return audioPlayer.get().getDurationInFrames() ?: 0
    }

    fun rewind(speed: ScrollSpeed) {
        if (isPlaying()) {
            resumeAfterScroll = true
            mediaToggle()
        }
        audioController?.rewind(speed)
    }

    fun fastForward(speed: ScrollSpeed) {
        if (isPlaying()) {
            resumeAfterScroll = true
            mediaToggle()
        }
        audioController?.fastForward(speed)
    }

    fun resumeMedia() {
        if (resumeAfterScroll) {
            mediaToggle()
            resumeAfterScroll = false
        }
    }

    fun mediaToggle() {
        if (audioController?.isPlayingProperty?.value == false) {
            /* trigger change to auto-scroll when it starts playing */
            val currentMarkerIndex = currentMarkerNumberProperty.value
            currentMarkerNumberProperty.set(-1)
            currentMarkerNumberProperty.set(currentMarkerIndex)
        }
        audioController?.toggle()
    }

    private fun isPlaying(): Boolean {
        return audioController?.isPlayingProperty?.value ?: false
    }
}
