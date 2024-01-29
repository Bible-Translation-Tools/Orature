/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.binding.IntegerBinding
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel

interface IMarkerViewModel : IWaveformViewModel {
    var markerModel: MarkerPlacementModel?
    val markers: ObservableList<MarkerItem>
    val markerCountProperty: IntegerBinding
    var audioController: AudioPlayerController?
    val currentMarkerNumberProperty: SimpleIntegerProperty

    var resumeAfterScroll: Boolean

    fun placeMarker() {
        markerModel?.let { markerModel ->
            markerModel.addMarker(waveformAudioPlayerProperty.get().getLocationInFrames())
            markers.setAll(markerModel.markerItems)
        }
    }

    fun deleteMarker(id: Int) {
        markerModel?.let { markerModel ->
            markerModel.deleteMarker(id)
            markers.setAll(markerModel.markerItems)
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
            markers.setAll(markerModel.markerItems)
        }
    }

    fun redoMarker() {
        markerModel?.let { markerModel ->
            markerModel.redo()
            markers.setAll(markerModel.markerItems)
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
