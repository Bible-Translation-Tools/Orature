package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.ViewModel

class ChunkingViewModel: ViewModel() {

    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio = SimpleObjectProperty<WavFile>()

}
