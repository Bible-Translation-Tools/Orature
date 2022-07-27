package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import tornadofx.*

class NavBar : View() {
    private val viewModel: NavBarViewModel by inject()

    override fun onDock() {
        super.onDock()
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    override val root = hbox(5) {
        style { padding = box(5.px) }
        button("OQuA") {
            action {
                viewModel.wbDataStore.activeWorkbookProperty.set(null)
            }
        }
        button(viewModel.projectTitleProperty){
            style { visibleWhen(booleanBinding(viewModel.projectTitleProperty) { value != null }) }
            action {
                viewModel.wbDataStore.activeChapterProperty.set(null)
            }
        }
        button(viewModel.chapterTitleProperty) {
            style { visibleWhen(booleanBinding(viewModel.wbDataStore.activeChapterProperty) { value != null }) }
        }
    }
}