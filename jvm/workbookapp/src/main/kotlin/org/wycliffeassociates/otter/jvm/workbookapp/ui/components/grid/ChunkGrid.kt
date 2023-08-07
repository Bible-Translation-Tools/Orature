package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid

import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

class ChunkGrid(list: List<ChunkViewData>) : GridPane() {

    init {
        gridpaneColumnConstraints {
            percentWidth = 100.0 / 3.0 // Three columns, each taking up 1/3 of the available width
        }

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
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                            addClass("chunk-item__icon")
                        }
                    } else {
                        FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply {
                            addClass("chunk-item__icon")
                        }
                    }
                }
            )

            chunk.selectedChunk.onChange {
                val selected = it == chunk.number
                this.togglePseudoClass("selected", selected)
                isFocusTraversable = !selected
                isMouseTransparent = selected
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