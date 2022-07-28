package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import tornadofx.*

class ProjectView : View() {
    private val viewModel: ProjectViewModel by inject()

    override fun onDock() {
        viewModel.dock()
    }

    override val root = listview(viewModel.chapters) {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        cellFragment(ChapterListCellFragment::class)
    }
}