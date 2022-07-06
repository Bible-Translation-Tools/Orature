/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.toggle.RadioButtonPane
import org.wycliffeassociates.otter.jvm.controls.toggle.ToggleButtonData
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.BookCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.BookCardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import java.text.MessageFormat

class BookSelection : View() {

    private val viewModel: BookWizardViewModel by inject()
    private val navigator: NavigationMediator by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["selectBook"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOK))
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("book-wizard__root")

            vbox {
                label(messages["selectBook"]) {
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
                        graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT).apply {
                            scaleXProperty().bind(settingsViewModel.orientationScaleProperty)
                        }
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
            hbox {
                addClass("book-wizard__resource-tab-group")
                add(
                    RadioButtonPane().apply {
                        addClass("wa-tab-pane")
                        buildResourceSelections(this)
                    }
                )
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
                    BookCell(viewModel.existingBooks) {
                        viewModel.selectedBookProperty.set(it.collection)
                    }
                }
                viewModel.searchQueryProperty.onChange {
                    it?.let { if (it.isNotBlank()) scrollTo(0) }
                }

                overrideDefaultKeyEventHandler {
                    val current = selectionModel.selectedItem
                    val availableItems = getAvailableBooks()
                    var index = availableItems.indexOf(current)
                    when (it) {
                        KeyCode.UP -> index--
                        KeyCode.DOWN -> index++
                    }
                    val item = availableItems.getOrElse(index) { current }
                    selectionModel.select(item)

                    virtualFlow().apply {
                        scrollTo(items.indexOf(item))
                    }
                }

                focusedProperty().onChange { focused ->
                    if (focused) {
                        val item = getAvailableBooks().firstOrNull()
                        val index = items.indexOf(item)
                        selectionModel.select(index)
                    }
                }
            }
        }
    }

    private fun buildResourceSelections(togglePane: RadioButtonPane) {
        viewModel.sourceCollections.onChange {
            val data = it.list.mapIndexed { index, resource ->
                val isFirst = index == 0
                ToggleButtonData(resource.slug, isFirst) {
                    viewModel.selectedSourceProperty.set(resource)
                }
            }
            togglePane.list.setAll(data)
        }
    }

    init {
        tryImportStylesheet(resources["/css/book-wizard.css"])
        tryImportStylesheet(resources["/css/filtered-search-bar.css"])
        tryImportStylesheet(resources["/css/book-card-cell.css"])
        tryImportStylesheet(resources["/css/confirm-dialog.css"])

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
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)

            viewModel.showProgressProperty.onChange {
                Platform.runLater { if (it) open() else close() }
            }
        }
    }

    private fun getAvailableBooks(): List<BookCardData> {
        return viewModel.filteredBooks.filter {
            viewModel.existingBooks
                .any { existing ->
                    matchedExistingBook(it, existing)
                }.not()
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.reset()
        viewModel.loadExistingProjects()
        viewModel.loadResources()
    }
}

fun matchedExistingBook(book: BookCardData, existingBook: Workbook): Boolean {
    return existingBook.target.slug == book.collection.slug &&
            (existingBook.sourceMetadataSlug ==
                    book.collection.resourceContainer?.identifier)
}