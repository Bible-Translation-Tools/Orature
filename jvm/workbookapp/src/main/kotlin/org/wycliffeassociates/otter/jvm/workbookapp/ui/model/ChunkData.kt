package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.getProperty
import tornadofx.property
import java.io.File

class ChunkData(
    val sort: Int,
    val title: String,
    val text: String
) {
    var player: IAudioPlayer? = null
    var file: File? = null

    var image: Image by property(null)
    val imageProperty = getProperty(ChunkData::image)

    var invertedImage: Image by property(null)
    val invertedImageProperty = getProperty(ChunkData::invertedImage)

    var imageLoading: Boolean by property(false)
    val imageLoadingProperty = getProperty(ChunkData::imageLoading)

    constructor(chunk: Chunk) : this(
        sort = chunk.sort,
        title = chunk.title,
        text = chunk.textItem.text
    )

    fun hasAudio(): Boolean {
        return file != null
    }
}