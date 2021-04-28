package org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.controls.stepper.stepper
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import tornadofx.*

class ProjectWizard : View() {
    override val root = borderpane {}
    private val wizardViewModel: ProjectWizardViewModel by inject()
    val wizardWorkspace = Workspace()
    private val navigator: NavigationMediator by inject()

    val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["create"].toLowerCase().capitalize())
        iconProperty.set(FontIcon(MaterialDesign.MDI_CREATION))
    }

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
        navigator.dock(this, breadCrumb)
    }

    override fun onUndock() {
        super.onUndock()
        wizardWorkspace.navigateBack()
        wizardViewModel.reset()
    }
}
