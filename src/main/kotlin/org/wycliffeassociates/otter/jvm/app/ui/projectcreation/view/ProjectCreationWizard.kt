package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view

import javafx.geometry.Insets
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectCollection
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import tornadofx.*

class ProjectCreationWizard : Wizard() {

    private val creationViewModel: ProjectCreationViewModel by inject()
    override val canGoNext = currentPageComplete
    init {
        showStepsHeader = false
        showSteps = false
        showHeader = true
        enableStepLinks = true
        root.bottom {
            buttonbar {
                padding = Insets(10.0)

                button(messages["back"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoBack.and(!creationViewModel.showOverlayProperty))
                    action {
                        creationViewModel.goBack(this@ProjectCreationWizard)
                    }
                }

                button(messages["next"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoNext.and(hasNext))
                    action {
                        next()
                    }
                }

                button(messages["cancel"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(!creationViewModel.showOverlayProperty)
                    action {
                        onCancel()
                    }
                }
            }
        }

        add(SelectLanguage::class)
        add(SelectCollection::class)

        creationViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                    creationViewModel.reset()
                    currentPage = pages[0]
                    close()
                }
            }
        }
    }

    override fun onCancel() {
        creationViewModel.reset()
        currentPage = pages[0]
        close()
    }
}