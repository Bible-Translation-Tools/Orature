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

interface IMarkerViewModel : IWaveformViewModel {
    var markerModel: VerseMarkerModel?
    val markers: ObservableList<ChunkMarkerModel>
    val markerCountProperty: IntegerBinding
    var audioController: AudioPlayerController?
    val currentMarkerNumberProperty: SimpleIntegerProperty

    var resumeAfterScroll: Boolean

    fun placeMarker() {
        markerModel?.let { markerModel ->
            markerModel.addMarker(waveformAudioPlayerProperty.get().getLocationInFrames())
            markers.setAll(markerModel.markers)
        }
    }

    fun deleteMarker(id: Int) {
        markerModel?.let { markerModel ->
            markerModel.deleteMarker(id)
            markers.setAll(markerModel.markers)
        }
    }

    fun moveMarker(id: Int, start: Int, end: Int) {
        markerModel?.moveMarker(id, start, end)
    }

    fun seekNext() {
        val wasPlaying = waveformAudioPlayerProperty.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerModel?.let { markerModel ->
            seek(markerModel.seekNext(waveformAudioPlayerProperty.get().getLocationInFrames()))
        } ?: run { seek(waveformAudioPlayerProperty.get().getLocationInFrames()) }
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun seekPrevious() {
        val wasPlaying = waveformAudioPlayerProperty.get().isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        markerModel?.let { markerModel ->
            seek(markerModel.seekPrevious(waveformAudioPlayerProperty.get().getLocationInFrames()))
        } ?: run { seek(waveformAudioPlayerProperty.get().getLocationInFrames()) }
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
        return waveformAudioPlayerProperty.value?.getLocationInFrames() ?: 0
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
