package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChapterListCellFragment: ListCellFragment<Chapter>() {
    private val wbDataStore: WorkbookDataStore by inject()

    private val chapterTitleProperty = Bindings.createStringBinding(
        { itemProperty.value?.title },
        itemProperty
    )

    override val root = button(chapterTitleProperty) {
        action {
            wbDataStore.activeChapterProperty.set(item)
            workspace.dock(find<ChapterView>())
        }
    }
}