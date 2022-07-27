package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import tornadofx.*

class ProjectView : View() {
    private val viewModel: ProjectViewModel by inject()

    override fun onDock() {
        super.onDock()
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    override val root = listview<Chapter> {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        itemsProperty().bind(viewModel.chaptersProperty)
        cellFragment(ChapterListCellFragment::class)
    }
}