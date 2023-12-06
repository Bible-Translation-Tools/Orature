package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.scene.layout.GridPane
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

    fun focusOnSelectedChapter() {
        lookupAll(":selected").firstOrNull()?.requestFocus()
    }

    fun updateChapterGridNodes() {
        children.clear()
        columnConstraints.clear()
        addChaptersToGrid()
    }

    private fun addChaptersToGrid() {
        list.forEachIndexed { index, chapter ->
            val node = StackPane().apply {
                button(chapter.number.toString()) {
                    addClass(
                        "btn", "btn--secondary", "btn--borderless", "chapter-grid__btn"
                    )
                    togglePseudoClass("selected", chapter.selected)
                    setOnAction {
                        selectChapter(chapter.number)
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