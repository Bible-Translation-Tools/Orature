package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.data.workbook.Chunk
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

    var imageLoading: Boolean by property(false)
    val imageLoadingProperty = getProperty(ChunkData::imageLoading)

    var isPlaying: Boolean by property(false)
    val isPlayingProperty = getProperty(ChunkData::isPlaying)

    var isRecordingPaused: Boolean by property(false)
    val isRecordingPausedProperty = getProperty(ChunkData::isRecordingPaused)

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

    override fun hashCode(): Int {
        return Objects.hash(
            sort,
            title,
            text
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChunkData

        if (sort != other.sort) return false
        if (title != other.title) return false
        if (text != other.text) return false

        return true
    }
}