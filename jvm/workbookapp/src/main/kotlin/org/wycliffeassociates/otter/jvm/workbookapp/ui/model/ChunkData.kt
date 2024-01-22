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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.getProperty
import tornadofx.property
import java.io.File
import java.util.*

data class ChunkData(
    val sort: Int,
    val title: String,
    val text: String
) {
    var file: File? = null
    var start: Int = -1
    var end: Int = 0
    var isDraft = false

    var image: Image by property(null)
    val imageProperty = getProperty(ChunkData::image)

    var invertedImage: Image by property(null)
    val invertedImageProperty = getProperty(ChunkData::invertedImage)

    var waveform: Drawable by property(null)
    val waveformProperty = getProperty(ChunkData::waveform)

    var volumeBar: Drawable by property(null)
    val volumeBarProperty = getProperty(ChunkData::volumeBar)

    var imageLoading: Boolean by property(false)
    val imageLoadingProperty = getProperty(ChunkData::imageLoading)

    var isPlaying: Boolean by property(false)
    val isPlayingProperty = getProperty(ChunkData::isPlaying)

    var isRecording: Boolean by property(false)
    val isRecordingProperty = getProperty(ChunkData::isRecording)

    var playbackPosition: Int by property(0)
    val playbackPositionProperty = getProperty(ChunkData::playbackPosition)

    var recordButtonText: String by property(messages["beginRecording"])
    val recordButtonTextProperty = getProperty(ChunkData::recordButtonText)

    var totalFrames: Int by property(0)
    val totalFramesProperty = getProperty(ChunkData::totalFrames)

    var onPlay: (ChunkData) -> Unit = {}
    var onOpenApp: (ChunkData) -> Unit = {}
    var onRecordAgain: (ChunkData) -> Unit = {}
    var onWaveformClicked: (ChunkData) -> Unit = {}
    var onRecord: (ChunkData) -> Unit = {}
    var onNext: (ChunkData) -> Unit = {}

    constructor(chunk: Chunk) : this(
        sort = chunk.sort,
        title = chunk.title,
        text = chunk.textItem.text
    )

    fun hasAudio(): Boolean {
        return file != null
    }
}