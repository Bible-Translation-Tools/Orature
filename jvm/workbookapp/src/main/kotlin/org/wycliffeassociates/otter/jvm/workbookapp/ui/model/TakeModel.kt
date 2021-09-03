package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer

data class TakeModel(
    val take: Take,
    val selected: Boolean,
    val audioPlayer: IAudioPlayer
)
