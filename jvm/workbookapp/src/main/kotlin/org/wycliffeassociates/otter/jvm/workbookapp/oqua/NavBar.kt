package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import tornadofx.*

class NavBar : View() {
    private val viewModel: NavBarViewModel by inject()

    override val root = hbox(5) {
        style { padding = box(5.px) }
        button("OQuA") {
            action {
                workspace.dock(find<HomeView>())
            }
        }
        button(viewModel.projectTitleProperty){
            visibleWhen(viewModel.projectTitleProperty.isNotNull)
            action {
                workspace.dock(find<ProjectView>())
            }
        }
        button(viewModel.chapterTitleProperty) {
            visibleWhen(viewModel.chapterTitleProperty.isNotNull)
        }
    }
}