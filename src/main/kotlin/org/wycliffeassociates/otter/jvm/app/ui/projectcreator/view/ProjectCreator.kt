package org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view

import javafx.geometry.Insets
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view.ProjectCreatorStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.viewmodel.ProjectCreatorViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view.fragments.SelectCollection
import tornadofx.*

class ProjectCreator : View() {
    override val root = borderpane {}
    private val creatorViewModel: ProjectCreatorViewModel by inject()
    val creationWorkspace = Workspace()

    init {
        importStylesheet<ProjectCreatorStyles>()


        root.addClass(AppStyles.appBackground)
        root.center {
            add(creationWorkspace)
            creationWorkspace.header.removeFromParent()
            creationWorkspace.dock(SelectLanguage())
        }
        root.bottom {
            buttonbar {
                padding = Insets(40.0)
                button(messages["cancel"]) {
                    addClass(ProjectCreatorStyles.wizardButton)
//                    enableWhen(creatorViewModel.showOverlayProperty)
                    action {
                        creatorViewModel.closeCreator()
                    }
                }
                button(messages["back"]) {
                    addClass(ProjectCreatorStyles.wizardButton)
                    enableWhen(creatorViewModel.canGoBack.and(!creatorViewModel.showOverlayProperty))
                    action {
                       creatorViewModel.goBack()
                    }
                }

                button(messages["next"]) {
                    addClass(ProjectCreatorStyles.wizardButton)
                    enableWhen(creatorViewModel.canGoNext.and(creatorViewModel.languagesValid()))
                    visibleProperty().bind(!creatorViewModel.languageConfirmed)
                    action {
                        creatorViewModel.goNext()
                    }
                }


            }
        }
        creatorViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                   workspace.dock<MainScreenView>()
                }
            }
        }
    }


    override fun onDock() {
        super.onDock()
        creatorViewModel.reset()
    }
}