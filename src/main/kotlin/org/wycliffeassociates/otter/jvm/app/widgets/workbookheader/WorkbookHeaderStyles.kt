package org.wycliffeassociates.otter.jvm.app.widgets.workbookheader

import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class WorkbookHeaderStyles : Stylesheet() {

    companion object {
        val workbookHeader by cssclass()
    }

    init {
        workbookHeader {
            padding = box(10.px, 100.px, 20.px, 70.px)
            backgroundColor += AppTheme.colors.white
        }
    }
}
