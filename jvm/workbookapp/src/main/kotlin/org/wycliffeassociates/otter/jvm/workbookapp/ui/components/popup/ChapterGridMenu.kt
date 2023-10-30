package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.MenuItem
import org.w3c.dom.Text
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterGridMenu : ContextMenu() {

    val chapterGridItemList: ObservableList<ChapterGridItemData> = observableListOf()

    init {
        val chapterGridOption = CustomMenuItem().apply {
            addClass("chapter-grid-context-menu-item")
            val chapterGrid = ChapterGrid(chapterGridItemList).apply {
                // Handle changes in showingProperty. Used to update completed icon for each chapter.
                chapterGridItemList.onChange {
                    updateChapterList()
                }
                prefWidth = 500.0
            }
            content = chapterGrid
        }

        addClass("chapter-grid-context-menu")
        isAutoHide = true
        items.setAll(
            chapterGridOption,
        )
    }
}
