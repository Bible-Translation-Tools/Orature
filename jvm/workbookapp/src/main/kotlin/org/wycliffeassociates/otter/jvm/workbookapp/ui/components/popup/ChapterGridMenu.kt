package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterGridMenu : ContextMenu() {

    private val chapterGridItemList: MutableList<ChapterGridItemData> = mutableListOf()
    private val chapterGrid = ChapterGrid(chapterGridItemList)

    init {
        val chapterGridOption = MenuItem().apply {
            addClass("chapter-grid-context-menu-item")
            graphic = chapterGrid
        }

        addClass("chapter-grid-context-menu")
        isAutoHide = true
        items.setAll(chapterGridOption)
    }

    fun updateChapterGrid(newChapterList: List<ChapterGridItemData>) {
        chapterGridItemList.clear()
        chapterGridItemList.addAll(newChapterList)
        chapterGrid.updateChapterGridNodes()
    }

}
