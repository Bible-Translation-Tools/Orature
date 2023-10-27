package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterGridMenu : ContextMenu() {

    val chapterList: ObservableList<Chapter> = observableListOf()
    val chapterGridItemList: ObservableList<ChapterGridItemData> = observableListOf()

    val onChapterSelectedProperty = SimpleObjectProperty<(Int) -> Unit>()
    var onChapterSelected by onChapterSelectedProperty

    init {

        chapterList.onChange {
            chapterGridItemList.setAll(emptyList())
            chapterGridItemList.setAll(chapterList.map {
                ChapterGridItemData(
                    it.sort,
                    SimpleBooleanProperty(false)
                )
            })
        }

        val chapterGridOption = CustomMenuItem().apply {
            addClass("chapter-grid-context-menu-item")
            val chapterGrid = ChapterGrid(chapterGridItemList).apply {
                prefWidth = 500.0
                selectedChapterIndexProperty.addListener { _, old, new ->
                    onChapterSelected(new.toInt())
                }
            }
            content = chapterGrid
        }

        addClass("chapter-grid-context-menu")
        isAutoHide = true
        items.setAll(chapterGridOption)
    }
}
