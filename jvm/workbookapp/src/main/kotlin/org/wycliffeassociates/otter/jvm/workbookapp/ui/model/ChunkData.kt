package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.device.IAudioPlayer

class ChunkData(val sort: Int, val title: String, val text: String) {
    var player: IAudioPlayer? = null
    constructor(chunk: Chunk) : this(
        sort = chunk.sort,
        title = chunk.title,
        text = chunk.textItem.text
    )
}