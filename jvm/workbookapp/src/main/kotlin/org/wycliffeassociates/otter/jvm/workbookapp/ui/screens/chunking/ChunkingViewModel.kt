package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.ViewModel
import tornadofx.onChange

class ChunkingViewModel: ViewModel() {

    private val green = Paint.valueOf("#1edd76")
    private val gray = Paint.valueOf("#0a337333")
    private val blue = Paint.valueOf("#015ad9")

    val consumeStepColor = SimpleObjectProperty<Paint>(Paint.valueOf("#0a337333"))
    val verbalizeStepColor = SimpleObjectProperty<Paint>(Paint.valueOf("#0a337333"))
    val chunkStepColor = SimpleObjectProperty<Paint>(Paint.valueOf("#0a337333"))

    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio = SimpleObjectProperty<WavFile>()

    init {
        titleProperty.onChange {
            when(it) {
                "Consume" -> {
                    consumeStepColor.set(blue)
                    verbalizeStepColor.set(gray)
                    chunkStepColor.set(gray)
                }
                "Verbalize" -> {
                    consumeStepColor.set(green)
                    verbalizeStepColor.set(blue)
                    chunkStepColor.set(gray)
                }
                "Chunking" -> {
                    consumeStepColor.set(green)
                    verbalizeStepColor.set(green)
                    chunkStepColor.set(blue)
                }
            }
        }
    }

}
