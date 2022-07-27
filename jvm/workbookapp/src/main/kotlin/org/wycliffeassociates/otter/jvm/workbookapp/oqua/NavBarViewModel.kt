package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NavBarViewModel: ViewModel() {
    val wbDataStore: WorkbookDataStore by inject()

    val projectTitleProperty = Bindings.createStringBinding(
        { wbDataStore.activeWorkbookProperty.value?.target?.title },
        wbDataStore.activeWorkbookProperty
    )

    val chapterTitleProperty = Bindings.createStringBinding(
        {
            wbDataStore.activeChapterProperty.value?.let { chapter ->
                "${chapter.label.capitalizeString()} ${chapter.title}"
            }
        },
        wbDataStore.activeChapterProperty
    )

    fun dock() {}
    fun undock() {}
}