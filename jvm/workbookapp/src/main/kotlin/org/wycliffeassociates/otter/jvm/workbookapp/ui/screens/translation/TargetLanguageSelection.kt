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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.LanguageSelectionViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel
import tornadofx.*

class TargetLanguageSelection : Fragment() {
    private val translationViewModel: TranslationViewModel by inject()
    private val viewModel = LanguageSelectionViewModel(translationViewModel.targetLanguages)
    private val navigator: NavigationMediator by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["targetLanguage"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_VOICE))
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("translation-wizard__root")

            label(messages["pickTargetLanguage"]) {
                addClass("translation-wizard__title")
            }
            add(
                FilteredSearchBar().apply {
                    leftIconProperty.set(FontIcon(MaterialDesign.MDI_VOICE))
                    promptTextProperty.set(messages["search"])
                    filterItems.bind(viewModel.menuItems) { it }
                    viewModel.searchQueryProperty.bindBidirectional(textProperty)
                }
            )
            listview(viewModel.sortedLanguages) {
                addClass("translation-wizard__list")
                vgrow = Priority.ALWAYS
                setCellFactory {
                    LanguageCell(
                        LanguageType.TARGET,
                        viewModel.anglicizedProperty,
                    ) {
                        translationViewModel.selectedTargetLanguageProperty.set(it)
                    }
                }
                viewModel.searchQueryProperty.onChange {
                    it?.let { if (it.isNotBlank()) scrollTo(0) }
                }

                overrideDefaultKeyEventHandler {
                    val current = selectionModel.selectedItem
                    val availableItems = viewModel.filteredLanguages
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
                        val item = viewModel.filteredLanguages.firstOrNull()
                        val index = items.indexOf(item)
                        selectionModel.select(index)
                    }
                }
            }
        }
    }

    init {
        tryImportStylesheet(resources["/css/translation-wizard.css"])
        tryImportStylesheet(resources["/css/language-card-cell.css"])
        tryImportStylesheet(resources["/css/filtered-search-bar.css"])
        tryImportStylesheet(resources["/css/confirm-dialog.css"])

        translationViewModel.targetLanguages.onChange {
            viewModel.regions.setAll(
                it.list
                    .distinctBy { language -> language.region }
                    .map { language -> language.region }
            )
            viewModel.setFilterMenu()
        }

        createProgressDialog()
    }

    private fun createProgressDialog() {
        confirmdialog {
            titleTextProperty.set(messages["createTranslationTitle"])
            messageTextProperty.set(messages["pleaseWaitCreatingTranslation"])
            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)

            translationViewModel.showProgressProperty.onChange {
                Platform.runLater { if (it) open() else close() }
            }
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.resetFilter()
        translationViewModel.loadTargetLanguages()
    }
}
