package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.chapterselector.chapterGrid
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class ChapterSelectorDemoView : View() {

    private val list = observableListOf(
        ChapterGridItemData(1, SimpleBooleanProperty(false)),
        ChapterGridItemData(2, SimpleBooleanProperty(true)),
        ChapterGridItemData(3, SimpleBooleanProperty(false)),
        ChapterGridItemData(4, SimpleBooleanProperty(true)),
        ChapterGridItemData(5, SimpleBooleanProperty(false)),
        ChapterGridItemData(6, SimpleBooleanProperty(true)),
        ChapterGridItemData(7, SimpleBooleanProperty(false)),
        ChapterGridItemData(8, SimpleBooleanProperty(false)),
        ChapterGridItemData(9, SimpleBooleanProperty(false)),
        ChapterGridItemData(10, SimpleBooleanProperty(false))
    )

    override val root = VBox().apply {
        maxWidth = 500.0

        chapterGrid(list)
    }

    init {
        tryImportStylesheet("/css/chapter-grid.css")
    }
}