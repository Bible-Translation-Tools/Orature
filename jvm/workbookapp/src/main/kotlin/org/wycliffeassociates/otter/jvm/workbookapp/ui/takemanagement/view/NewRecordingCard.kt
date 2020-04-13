package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import tornadofx.*

class NewRecordingCard(val action: () -> Unit) : VBox() {
    init {
        with(this) {
            spacing = 10.0
            alignment = Pos.CENTER
            addClass(RecordScriptureStyles.newTakeCard)
            label(FX.messages["newTake"])
            button(FX.messages["record"], AppStyles.recordIcon("25px")) {
                action {
                    action()
                }
            }
        }
    }
}