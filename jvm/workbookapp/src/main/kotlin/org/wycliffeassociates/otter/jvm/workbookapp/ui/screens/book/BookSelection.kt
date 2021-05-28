package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.BookCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookWizardViewModel
import tornadofx.*
import java.text.MessageFormat

class BookSelection : Fragment() {

    private val viewModel: BookWizardViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["newBook"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOK))
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("book-wizard__root")

            vbox {
                label(messages["chooseBook"]) {
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
            add(
                FilteredSearchBar().apply {
                    leftIconProperty.set(FontIcon(MaterialDesign.MDI_BOOK))
                    promptTextProperty.set(messages["search"])
                    filterItems.bind(viewModel.menuItems) { it }
                    viewModel.searchQueryProperty.bindBidirectional(textProperty)
                }
            )
            listview(viewModel.filteredBooks) {
                addClass("book-wizard__list")
                vgrow = Priority.ALWAYS
                setCellFactory {
                    BookCell(viewModel.projectTypeProperty, viewModel.existingBooks) {
                        viewModel.selectedBookProperty.set(it)
                    }
                }
                viewModel.searchQueryProperty.onChange {
                    it?.let { if (it.isNotBlank()) scrollTo(0) }
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/book-wizard.css").toExternalForm())
        createProgressDialog()
    }

    private fun createProgressDialog() {
        confirmdialog {
            titleTextProperty.bind(
                viewModel.activeProjectTitleProperty.stringBinding {
                    it?.let {
                        MessageFormat.format(
                            messages["createProjectTitle"],
                            messages["createAlt"],
                            it
                        )
                    }
                }
            )
            messageTextProperty.set(messages["pleaseWaitCreatingProject"])
            backgroundImageFileProperty.bind(viewModel.activeProjectCoverProperty)
            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)

            viewModel.showProgressProperty.onChange {
                Platform.runLater { if (it) open() else close() }
            }
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.reset()
        viewModel.loadResources()
    }
}
