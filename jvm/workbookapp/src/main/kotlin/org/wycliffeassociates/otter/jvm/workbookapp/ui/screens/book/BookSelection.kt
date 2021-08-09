/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
//    @Inject lateinit var directoryProvider: IDirectoryProvider

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
                        textProperty().bind(
                            viewModel.translationProperty.stringBinding {
                                it?.sourceLanguage?.name
                            }
                        )
                    }
                    label {
                        addClass("book-wizard__divider")
                        graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT)
                    }
                    label {
                        addClass("book-wizard__language")
                        graphic = FontIcon(MaterialDesign.MDI_VOICE)
                        textProperty().bind(
                            viewModel.translationProperty.stringBinding {
                                it?.targetLanguage?.name
                            }
                        )
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
                    BookCell(
                        viewModel.projectTypeProperty,
                        viewModel.existingBooks
                    ) {
                        viewModel.selectedBookProperty.set(it.collection)
                    }
                }
                viewModel.searchQueryProperty.onChange {
                    it?.let { if (it.isNotBlank()) scrollTo(0) }
                }
            }
        }
    }

    init {
        importStylesheet(resources.get("/css/book-wizard.css"))
        importStylesheet(resources.get("/css/filtered-search-bar.css"))
        importStylesheet(resources.get("/css/book-card-cell.css"))
        importStylesheet(resources.get("/css/confirm-dialog.css"))

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
        viewModel.loadExistingProjects()
        viewModel.loadResources()
    }
}
