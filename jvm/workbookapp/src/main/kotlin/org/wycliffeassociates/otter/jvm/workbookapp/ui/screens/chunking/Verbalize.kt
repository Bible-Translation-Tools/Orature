package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.geometry.Pos
import tornadofx.*

class Verbalize : View() {

    val chunkvm: ChunkingViewModel by inject()

    val vm: VerbalizeViewModel by inject()

    override fun onDock() {
        super.onDock()
        chunkvm.titleProperty.set("Verbalize")
        chunkvm.stepProperty.set("Record a summary of what you heard in the chapter. You may use this later to help remember the story.")
    }

    override val root = borderpane {
        center = hbox {
            alignment = Pos.CENTER

            button("rec") { action { vm.toggle() } }
            button("re-rec") { action { vm.reRec() } }
            button("play") { action { vm.playToggle() } }
        }
    }
}
