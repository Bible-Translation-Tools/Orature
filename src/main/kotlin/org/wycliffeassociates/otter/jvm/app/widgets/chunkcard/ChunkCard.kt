package org.wycliffeassociates.otter.jvm.app.widgets.chunkcard


import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Chunk
import tornadofx.*
import tornadofx.FX.Companion.messages

class ChunkCard(initialChunk: Chunk? = null) : VBox() {
    var actionButton = JFXButton()
    var chunk: Chunk by property(initialChunk)

    fun chunkProperty() = getProperty(ChunkCard::chunk)

    init {
        importStylesheet<ChunkCardStyles>()
        addClass(ChunkCardStyles.defaultChunkCard)
        label(chunkProperty().stringBinding {
            "${messages[it?.labelKey ?: ""]} ${it?.start ?: ""}"
        }) {
            vgrow = Priority.ALWAYS
            maxHeight = Double.MAX_VALUE
            addClass(ChunkCardStyles.titleLabel)
        }
        label(chunkProperty().stringBinding {
            if (it?.selectedTake != null) "${messages["take"]} ${it.selectedTake?.number ?: ""}" else ""
        }) {
            vgrow = Priority.ALWAYS
            maxHeight = Double.MAX_VALUE
            addClass(ChunkCardStyles.selectedTakeLabel)
        }
        actionButton.isDisableVisualFocus = true
        add(actionButton)
    }
}

fun chunkcard(verse: Chunk, init: ChunkCard.() -> Unit): ChunkCard {
    val vc = ChunkCard(verse)
    vc.init()
    return vc
}