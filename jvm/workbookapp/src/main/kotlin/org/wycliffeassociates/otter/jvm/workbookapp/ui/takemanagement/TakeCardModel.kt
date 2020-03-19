package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import java.time.LocalDate

enum class TakeCardType {
    EMPTY,
    NEW,
    TAKE
}

data class TakeModel(
    val take: Take,
    val audioPlayer: IAudioPlayer,
    val editText: String,
    val deleteText: String,
    val playText: String
)