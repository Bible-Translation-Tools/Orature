package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view

import javafx.geometry.Insets
import org.wycliffeassociates.otter.jvm.app.ui.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments.SelectCollection
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel.ProjectWizardViewModel
import tornadofx.*

class ProjectWizard : Wizard() {

    private val wizardViewModel: ProjectWizardViewModel by inject()
    override val canGoNext = currentPageComplete
    init {
        importStylesheet<ProjectWizardStyles>()
        showStepsHeader = false
        showSteps = false
        showHeader = true
        enableStepLinks = true
        showHeader = false
        root.addClass(AppStyles.appBackground)
        root.bottom {
            buttonbar {
                padding = Insets(10.0)

                button(messages["back"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoBack.and(!wizardViewModel.showOverlayProperty))
                    action {
                        wizardViewModel.goBack(this@ProjectWizard)
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
                    enableWhen(!wizardViewModel.showOverlayProperty)
                    action {
                        onCancel()
                    }
                }
            }
        }

        add(SelectLanguage::class)
        add(SelectCollection::class)

        wizardViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                    wizardViewModel.reset()
                    currentPage = pages[0]
                    close()
                }
            }
        }
    }

    override fun onCancel() {
        wizardViewModel.reset()
        currentPage = pages[0]
        close()
    }
}