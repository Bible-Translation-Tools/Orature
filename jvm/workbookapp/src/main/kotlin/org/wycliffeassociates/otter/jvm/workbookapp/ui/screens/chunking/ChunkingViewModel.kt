package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.ViewModel

class ChunkingViewModel: ViewModel() {

    val sourceAudio = SimpleObjectProperty<WavFile>()


}
