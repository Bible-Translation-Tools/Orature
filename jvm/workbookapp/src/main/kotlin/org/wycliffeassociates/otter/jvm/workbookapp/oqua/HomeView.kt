package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import tornadofx.*

class HomeView : View() {
    private val viewModel: HomeViewModel by inject()

    override fun onDock() {
        super.onDock()
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    override val root = listview(viewModel.tCards) {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        cellFragment(TCardListCellFragment::class)
    }
}