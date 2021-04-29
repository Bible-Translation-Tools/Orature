package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.Wizard

class ChunkingWizard: Wizard() {

    init {
        add<Consume>()
        add<Verbalize>()
    }

    override fun closeWizard() {
        complete.set(false)
        workspace.navigateBack()
    }
}
