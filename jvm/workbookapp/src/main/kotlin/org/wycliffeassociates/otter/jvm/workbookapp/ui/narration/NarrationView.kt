package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class NarrationView : View() {
    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }

    override val root = stackpane {
        addClass(org.wycliffeassociates.otter.common.data.ColorTheme.LIGHT.styleClass)

        borderpane {
            top<NarrationHeader>()
            center<NarrationBody>()
            bottom<NarrationFooter>()
        }
    }
}

class NarrationViewViewModel : ViewModel() {
    val recordStartProperty = SimpleBooleanProperty()
    val recordPauseProperty = SimpleBooleanProperty()
    val recordResumeProperty = SimpleBooleanProperty()
    val isRecordingProperty = SimpleBooleanProperty()
    val isRecordingAgainProperty = SimpleBooleanProperty()

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()
}