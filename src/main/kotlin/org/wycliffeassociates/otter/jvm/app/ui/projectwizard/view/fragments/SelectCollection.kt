package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments

import javafx.application.Platform
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.wizardcard
import tornadofx.*

class SelectCollection : Fragment() {
    private val viewModel: ProjectWizardViewModel by inject()
    override val root = scrollpane {
        isFitToHeight = true
        isFitToWidth = true
        addClass(AppStyles.appBackground)
        flowpane {
            addClass(AppStyles.appBackground)
            addClass(ProjectWizardStyles.collectionFlowPane)
            bindChildren(viewModel.collections) {
                hbox {
                    wizardcard {
                        var projectExists = false
                        if (it.labelKey == "project") { //only check if project exists when we are at project level
                            projectExists = viewModel.doesProjectExist(it)
                        }
                        addClass(ProjectWizardStyles.wizardCard)
                        text = it.titleKey
                        buttonText = messages["select"]
                        cardButton.apply {
                            text = messages["select"]
                            action {
                                viewModel.doOnUserSelection(it)
                            }
                            isDisable = projectExists
                        }
                        graphicContainer.apply {
                            addClass(ProjectWizardStyles.wizardCardGraphicsContainer)
                            add(ProjectWizardStyles.resourceGraphic(it.slug))
                        }
                    }
                }
            }
            hbox {
                if (viewModel.collections.isEmpty()) { //if user selects resource with no children initially
                    label(messages["noResources"]) {
                        addClass(ProjectWizardStyles.noResource)
                    }
                }
                viewModel.collections.onChange {
                    clear()
                    if (viewModel.collections.isEmpty()) {
                        label(messages["noResources"]) {
                            addClass(ProjectWizardStyles.noResource)
                        }
                    }
                }
            }
        }
        val dialog = progressdialog {
            text = messages["pleaseWaitCreatingProject"]
            root.addClass(AppStyles.progressDialog)
        }
        viewModel.showOverlayProperty.onChange { it: Boolean ->
            Platform.runLater { if (it) dialog.open() else dialog.close() }
        }

    }
}