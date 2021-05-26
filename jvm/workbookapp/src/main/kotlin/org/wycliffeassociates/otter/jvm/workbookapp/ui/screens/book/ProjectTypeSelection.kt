package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectType
import tornadofx.*

class ProjectTypeSelection : Fragment() {

    private val viewModel: BookWizardViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            viewModel.projectTypeProperty.stringBinding {
                it?.let {
                    messages[it.value]
                } ?: messages["projectType"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_LINK_OFF))
        onClickAction {
            viewModel.projectTypeProperty.value?.let {
                navigator.back()
            }
        }
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("book-wizard__root")
            isFillWidth = false

            vbox {
                label(messages["chooseProjectType"]) {
                    addClass("book-wizard__title")
                }
                hbox {
                    addClass("book-wizard__language-card")
                    label {
                        addClass("book-wizard__language")
                        graphic = FontIcon(Material.HEARING)
                        textProperty().bind(viewModel.sourceLanguageProperty.stringBinding { it?.name })
                    }
                    label {
                        addClass("book-wizard__divider")
                        graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT)
                    }
                    label {
                        addClass("book-wizard__language")
                        graphic = FontIcon(MaterialDesign.MDI_VOICE)
                        textProperty().bind(viewModel.targetLanguageProperty.stringBinding { it?.name })
                    }
                }
            }
            region {
                addClass("book-wizard__header-gap")
            }
            hbox {
                addClass("book-wizard__project-type-card", "book-wizard__project-type-card--primary")
                label {
                    addClass("book-wizard__project-type-icon")
                    graphic = FontIcon(MaterialDesign.MDI_LINK_OFF)
                }
                vbox {
                    spacing = 10.0
                    label(messages["draftingProject"]) {
                        addClass("book-wizard__project-type-title")
                    }
                    label(messages["draftingProjectInfo"]) {
                        addClass("book-wizard__project-type-info")
                        isWrapText = true
                    }
                    button(messages["select"]) {
                        addClass("btn", "btn--cta")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        action {
                            viewModel.projectTypeProperty.set(ProjectType.DRAFTING)
                        }
                    }
                }
            }
            hbox {
                addClass("book-wizard__project-type-card", "book-wizard__project-type-card--secondary")
                label {
                    addClass("book-wizard__project-type-icon")
                    graphic = FontIcon(MaterialDesign.MDI_LINK_OFF)
                }
                vbox {
                    spacing = 10.0
                    label(messages["finalRecordingProject"]) {
                        addClass("book-wizard__project-type-title")
                    }
                    label(messages["finalRecordingProjectInfo"]) {
                        addClass("book-wizard__project-type-info")
                        isWrapText = true
                    }
                    button(messages["select"]) {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        action {
                            viewModel.projectTypeProperty.set(ProjectType.FINAL_RECORDING)
                        }
                    }
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/book-wizard.css").toExternalForm())
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.reset()
    }
}
