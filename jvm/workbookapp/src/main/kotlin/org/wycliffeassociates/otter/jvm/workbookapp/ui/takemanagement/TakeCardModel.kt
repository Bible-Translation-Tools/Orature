package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer

enum class TakeCardType {
    EMPTY,
    NEW,
    TAKE
}

data class TakeCardModel(
    val take: Take,
    var selected: Boolean,
    val audioPlayer: IAudioPlayer,
    val editText: String,
    val deleteText: String,
    val markerText: String,
    val playText: String,
    val pauseText: String
)
