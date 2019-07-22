package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.RecordScriptureStyles
import tornadofx.*
import tornadofx.FX.Companion.messages

class RecordNewTakeCard(recordNewTake: () -> Unit): VBox() {
    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<AppStyles>()

        spacing = 10.0
        alignment = Pos.CENTER
        addClass(RecordScriptureStyles.newTakeCard)
        label(messages["newTake"])
        button(messages["record"], AppStyles.recordIcon("25px")) {
            action {
                recordNewTake()
            }
        }
    }
}