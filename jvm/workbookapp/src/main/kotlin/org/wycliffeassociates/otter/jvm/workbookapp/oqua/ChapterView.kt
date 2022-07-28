package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import tornadofx.*

class ChapterView : View() {
    private val viewModel: ChapterViewModel by inject()

    override fun onDock() {
        super.onDock()
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        simpleaudioplayer {
            hgrow = Priority.ALWAYS
            playerProperty.bind(viewModel.audioPlayerProperty)
            visibleWhen(playerProperty.isNotNull)
            managedProperty().bind(visibleProperty())
        }
        listview(viewModel.questions) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            cellFragment(TQListCellFragment::class)
        }
    }
}