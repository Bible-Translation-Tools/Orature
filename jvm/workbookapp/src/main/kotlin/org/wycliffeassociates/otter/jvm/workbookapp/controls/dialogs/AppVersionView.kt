package org.wycliffeassociates.otter.jvm.workbookapp.controls.dialogs

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*

class AppVersionView() : View() {
    override val root = vbox {
        prefHeight = 100.0
        prefWidth = 200.0
        alignment = Pos.CENTER
        val info = AppInfo()
        label("${messages["appName"]}") {
            style {
                fontSize = 24.px
            }
        }
        label("${info.getVersion()}") {
            style {
                fontSize = 16.px
            }
        }
    }
}
