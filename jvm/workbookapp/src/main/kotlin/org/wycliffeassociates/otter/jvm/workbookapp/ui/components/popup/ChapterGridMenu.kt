package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterGridMenu : ContextMenu() {

    val chapterList: ObservableList<Chapter> = observableListOf()
    val chapterGridItemList: ObservableList<ChapterGridItemData> = observableListOf()

    init {

        // Handle changes in chapterList. Isn't necessary, but prevents the list from updating chapter count on open.
        chapterList.onChange {
            updateChapterGridItemList()
        }

        // Handle changes in showingProperty. Used to update completed icon for each chapter.
        showingProperty().onChange { showing ->
            updateChapterGridItemList()
        }

        val chapterGridOption = CustomMenuItem().apply {
            addClass("chapter-grid-context-menu-item")
            val chapterGrid = ChapterGrid(chapterGridItemList).apply {
                prefWidth = 500.0
                selectedChapterIndexProperty.addListener { _, old, new ->
                    selectChapter(new.toInt())
                }
            }
            content = chapterGrid
        }

        addClass("chapter-grid-context-menu")
        isAutoHide = true
        items.setAll(chapterGridOption)
    }

    fun selectChapter(chapterIndex: Int) {
        chapterList
            .elementAtOrNull(chapterIndex)
            ?.let { chapter ->
                FX.eventbus.fire(OpenChapterEvent(chapterList[chapterIndex]))
            }
    }

    private fun updateChapterGridItemList() {
        chapterGridItemList.setAll(chapterList.map {
            ChapterGridItemData(
                it.sort,
                SimpleBooleanProperty(it.hasSelectedAudio())
            )
        })
    }
}
