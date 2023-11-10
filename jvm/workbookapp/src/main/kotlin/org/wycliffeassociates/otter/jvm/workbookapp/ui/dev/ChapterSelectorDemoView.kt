package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class ChapterSelectorDemoView : View() {

    private val list = listOf(
        ChapterGridItemData(1, false),
        ChapterGridItemData(2, true),
        ChapterGridItemData(3, false),
        ChapterGridItemData(4, true),
        ChapterGridItemData(5, false),
        ChapterGridItemData(6, true),
        ChapterGridItemData(7, false),
        ChapterGridItemData(8, false),
        ChapterGridItemData(9, false),
        ChapterGridItemData(10, false)
    )

    override val root = VBox().apply {
        maxWidth = 500.0

        add(ChapterGrid(list))
    }

    init {
        tryImportStylesheet("/css/chapter-grid.css")
    }
}