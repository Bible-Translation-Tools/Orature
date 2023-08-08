package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

private const val GRID_COLUMNS = 5

class ChapterGrid(list: List<ChapterGridItemData>) : GridPane() {

    init {
        addClass("chapter-grid")
        list.forEachIndexed { index, chapter ->
            val node = StackPane().apply {
                button(chapter.number.toString()) {
                    addClass(
                        "btn", "btn--secondary", "btn--borderless", "chapter-grid__btn"
                    )
                    prefWidthProperty().bind(
                        this@ChapterGrid.widthProperty().divide(GRID_COLUMNS.toDouble())
                    )
                }
                hbox {
                    addClass("chapter-grid__icon-alignment-box")
                    add(
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("complete-icon") }
                    )
                    isMouseTransparent = true
                    isPickOnBounds = false
                    visibleWhen { chapter.completedProperty }
                    managedWhen(visibleProperty())
                }
            }
            this.add(node, index % GRID_COLUMNS, index / GRID_COLUMNS)
        }
    }
}