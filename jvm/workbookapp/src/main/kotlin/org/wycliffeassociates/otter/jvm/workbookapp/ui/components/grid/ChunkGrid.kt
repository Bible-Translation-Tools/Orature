package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid

import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

private const val GRID_COLUMNS = 3

class ChunkGrid(list: List<ChunkViewData>) : GridPane() {

    init {
        hgrow = Priority.ALWAYS

        list.forEachIndexed { index, chunk ->
            val btn = createChunkButton(chunk)
            btn.prefWidthProperty().bind(this.widthProperty().divide(GRID_COLUMNS.toDouble()))
            this.add(btn, index % GRID_COLUMNS, index / GRID_COLUMNS)
        }
    }

    private fun createChunkButton(chunk: ChunkViewData): Button {
        return Button(chunk.number.toString()).apply {
            addClass("btn", "btn--secondary", "btn--borderless", "chunk-item")

            graphicProperty().bind(
                objectBinding(chunk.completedProperty, chunk.selectedChunkProperty) {
                    this.togglePseudoClass("completed", chunk.completedProperty.value)
                    when {
                        chunk.completedProperty.value -> FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                            addClass("chunk-item__icon")
                        }
                        chunk.selectedChunkProperty.value == chunk.number -> FontIcon(MaterialDesign.MDI_BOOKMARK).apply {
                            addClass("chunk-item__icon")
                        }
                        else -> FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply {
                            addClass("chunk-item__icon")
                        }
                    }
                }
            )

            chunk.selectedChunkProperty.onChange {
                val selected = it == chunk.number
                this.togglePseudoClass("selected", selected)
                isFocusTraversable = !selected
                isMouseTransparent = selected
            }

            action {
                chunk.selectedChunkProperty.set(chunk.number)
            }
        }
    }
}

fun EventTarget.chunkGrid(
    list: List<ChunkViewData>,
    op: ChunkGrid.() -> Unit = {}
) = ChunkGrid(list).attachTo(this, op)