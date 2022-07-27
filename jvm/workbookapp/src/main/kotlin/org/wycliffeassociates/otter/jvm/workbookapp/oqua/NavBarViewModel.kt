package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NavBarViewModel: ViewModel() {
    val wbDataStore: WorkbookDataStore by inject()

    val projectTitleProperty = stringBinding(wbDataStore.activeWorkbookProperty) {
        value?.target?.title
    }

    val chapterTitleProperty = stringBinding(wbDataStore.activeChapterProperty) {
        "${value?.label?.capitalizeString()} ${value?.title}"
    }

    fun dock() {}
    fun undock() {}
}