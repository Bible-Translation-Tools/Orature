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

    private val chapterGridItemList: MutableList<ChapterGridItemData> = mutableListOf()
    private val chapterGrid = ChapterGrid(chapterGridItemList)

    init {
        val chapterGridOption = CustomMenuItem().apply {
            addClass("chapter-grid-context-menu-item")
            chapterGrid.apply {
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

    fun updateChapterGrid(newChapterList: List<ChapterGridItemData>) {
        chapterGridItemList.clear()
        chapterGridItemList.addAll(newChapterList)
        chapterGrid.updateChapterGridNodes()
    }

}
