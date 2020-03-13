package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement

import org.wycliffeassociates.otter.common.device.IAudioPlayer
import java.time.LocalDate

enum class TakeCardType {
    EMPTY,
    NEW,
    TAKE
}

data class TakeModel(
    val number: String,
    val timestamp: LocalDate,
    val audioPlayer: IAudioPlayer,
    val editText: String,
    val deleteText: String,
    val playText: String
)