package org.wycliffeassociates.otter.jvm.app.widgets


import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Chunk
import tornadofx.*
import tornadofx.FX.Companion.messages
import tornadofx.Stylesheet.Companion.root

class ChunkCard(val chunk: Chunk) : VBox() {
    var actionButton = JFXButton()

    init {
        with(root) {
            alignment = Pos.CENTER
            spacing = 10.0
            label("${messages[chunk.labelKey]} ${chunk.start}") {
                vgrow = Priority.ALWAYS
                maxHeight = Double.MAX_VALUE
                addClass("title")
            }
            chunk.selectedTake?.let {
                label("${messages["take"]} ${it.number}") {
                    vgrow = Priority.ALWAYS
                    maxHeight = Double.MAX_VALUE
                    addClass("selected-take")
                }
            }
            add(actionButton)
        }
    }
}

fun chunkcard(verse: Chunk, init: ChunkCard.() -> Unit): ChunkCard {
    val vc = ChunkCard(verse)
    vc.init()
    return vc
}