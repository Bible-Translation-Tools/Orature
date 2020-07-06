package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.progressstepper.stepper
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.viewmodel.ProjectWizardViewModel
import tornadofx.*

class ProjectWizard : View() {
    override val root = borderpane {}
    private val wizardViewModel: ProjectWizardViewModel by inject()
    val wizardWorkspace = Workspace()

    data class stepItem(
        val stepText: String,
        val stepGraphic: Node,
        val completedText: SimpleStringProperty
    )

    val stepList: List<stepItem> = listOf(
        stepItem(
            stepText = messages["selectLanguage"],
            stepGraphic = ProjectWizardStyles.translateIcon(),
            completedText = wizardViewModel.languageCompletedText
        ),
        stepItem(
            stepText = messages["selectResource"],
            stepGraphic = ProjectWizardStyles.resourceIcon(),
            completedText = wizardViewModel.resourceCompletedText
        ),
        stepItem(
            stepText = messages["selectBook"],
            stepGraphic = ProjectWizardStyles.bookIcon(),
            completedText = wizardViewModel.bookCompletedText
        )
    )

    init {
        importStylesheet<ProjectWizardStyles>()
        root.addClass(AppStyles.appBackground)

        root.top {
            vbox(32.0) {
                alignment = Pos.CENTER
                paddingAll = 24.0
                add(
                    stepper {
                        stepList.forEachIndexed { index, stepItem ->
                            add(
                                step(separator = index < stepList.size - 1) {
                                    stepText = stepItem.stepText
                                    stepGraphic = stepItem.stepGraphic
                                    completedTextProperty.bind(stepItem.completedText)
                                }
                            )
                        }
                    }
                )
            }
        }
        root.center {
            add(wizardWorkspace)
            wizardWorkspace.header.removeFromParent()
            wizardWorkspace.dock(SelectLanguage())
        }
        root.bottom {
            buttonbar {
                paddingAll = 40.0
                button(messages["cancel"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    action {
                        wizardViewModel.closeWizard()
                    }
                }
                button(messages["back"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(wizardViewModel.canGoBack and !wizardViewModel.showOverlayProperty)
                    action {
                        wizardViewModel.goBack()
                    }
                }
                button(messages["next"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(wizardViewModel.languagesValid() and !wizardViewModel.languageConfirmed)
                    action {
                        wizardViewModel.goNext()
                    }
                }
            }
        }
        wizardViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                    wizardViewModel.closeWizard()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        wizardViewModel.reset()
    }

    override fun onUndock() {
        super.onUndock()
        wizardWorkspace.navigateBack()
        wizardViewModel.reset()
    }
}
