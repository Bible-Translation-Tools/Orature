package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid

import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.action
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.objectBinding
import tornadofx.onChange
import tornadofx.togglePseudoClass

class ChunkGrid(list: List<ChunkViewData>) : GridPane() {

    private val chunkCompletedIcon = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
        addClass("chunk-item__icon")
    }

    private val chunkIcon =  FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply {
        addClass("chunk-item__icon")
    }

    init {
        list.forEachIndexed { index, chunk ->
            val btn = createChunkButton(chunk)
            this.add(btn, index % 3, index / 3)
        }
    }

    private fun createChunkButton(chunk: ChunkViewData): Button {
        return Button(chunk.number.toString()).apply {
            addClass("btn", "btn--secondary", "btn--borderless", "chunk-item")

            graphicProperty().bind(
                chunk.completedProperty.objectBinding {
                    this.togglePseudoClass("completed", it == true)
                    if (it == true) {
                        chunkCompletedIcon
                    } else {
                        chunkIcon
                    }
                }
            )

            chunk.selectedChunk.onChange {
                this.togglePseudoClass("selected", it == chunk.number)
            }

            action {
                chunk.selectedChunk.set(chunk.number)
            }
        }
    }
}

fun EventTarget.chunkGrid(
    list: List<ChunkViewData>,
    op: ChunkGrid.() -> Unit = {}
) = ChunkGrid(list).attachTo(this, op)