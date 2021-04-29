package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.geometry.Pos
import tornadofx.*

class Verbalize : View() {

    val vm: VerbalizeViewModel by inject()

    override val root = borderpane {
        center = hbox {

        }
        bottom = hbox {
            alignment = Pos.CENTER

            button("rec") { action { vm.toggle() } }
            button("re-rec") { action { vm.reRec() } }
            button("play") { action { vm.playToggle() } }
        }
    }
}
