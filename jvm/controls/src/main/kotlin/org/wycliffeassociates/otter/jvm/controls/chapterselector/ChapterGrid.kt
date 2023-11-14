package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

private const val GRID_COLUMNS = 5

class ChapterGrid(val list: List<ChapterGridItemData>) : GridPane() {

    init {
        addClass("chapter-grid")
        addChaptersToGrid()
    }

    private fun selectChapter(chapterIndex: Int) {
        FX.eventbus.fire(OpenChapterEvent(chapterIndex))
    }

    fun updateChapterGridNodes() {
        children.clear()
        addChaptersToGrid()
    }

    private fun addChaptersToGrid() {
        list.forEachIndexed { index, chapter ->
            val node = StackPane().apply {
                addClass("chapter-grid__item")
                button(chapter.number.toString()) {
                    addClass(
                        "btn", "btn--secondary", "btn--borderless", "chapter-grid__btn"
                    )
                    prefWidthProperty().bind(
                        this@ChapterGrid.widthProperty().divide(GRID_COLUMNS.toDouble())
                    )
                    setOnAction {
                        selectChapter(index + 1)
                    }
                }
                hbox {
                    addClass("chapter-grid__icon-alignment-box")
                    add(
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("complete-icon") }
                    )
                    isMouseTransparent = true
                    isPickOnBounds = false
                    visibleProperty().set(chapter.completed)
                    managedProperty().set(chapter.completed)
                }
            }
            this.add(node, index % GRID_COLUMNS, index / GRID_COLUMNS)
        }
    }
}