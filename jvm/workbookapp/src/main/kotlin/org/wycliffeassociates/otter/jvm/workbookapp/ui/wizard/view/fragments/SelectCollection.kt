package org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.fragments

import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.CoverArtAccessor
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.controls.card.wizardcard
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
            bindChildren(viewModel.collections) { collection ->
                hbox {
                    wizardcard {
                        var projectExists = false
                        // only check if project exists when we are at project level
                        if (collection.labelKey == "project") {
                            projectExists = viewModel.doesProjectExist(collection)
                        }
                        addClass(ProjectWizardStyles.wizardCard)
                        text = collection.titleKey
                        buttonText = messages["select"]
                        cardButton.apply {
                            text = messages["select"]
                            action {
                                viewModel.doOnUserSelection(collection)
                            }
                            isDisable = projectExists
                        }
                        graphicContainer.apply {
                            val iv = getImage(collection)
                            iv?.let {
                                add(iv)
                            } ?: run {
                                addClass(ProjectWizardStyles.wizardCardGraphicsContainer)
                                add(ProjectWizardStyles.resourceGraphic(collection.slug))
                            }
                        }
                    }
                }
            }
            hbox {
                if (viewModel.collections.isEmpty()) { // if user selects resource with no children initially
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
        }
        viewModel.showOverlayProperty.onChange { it: Boolean ->
            Platform.runLater { if (it) dialog.open() else dialog.close() }
        }
    }

    fun getImage(collection: Collection): ImageView? {
        val accessor = CoverArtAccessor(collection.resourceContainer!!, collection.slug)
        val file = accessor.getArtwork()
        return file?.let {
            val iv = ImageView(Image(file.inputStream()))
            iv.fitWidthProperty().set(250.0)
            iv.fitHeightProperty().set(250.0)
            iv
        }
    }
}
