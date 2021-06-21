package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.LanguageSelectionViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel
import tornadofx.*

class TargetLanguageSelection : Fragment() {
    private val translationViewModel: TranslationViewModel by inject()
    private val viewModel = LanguageSelectionViewModel(translationViewModel.targetLanguages)
    private val navigator: NavigationMediator by inject()

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
            listview(viewModel.filteredLanguages) {
                addClass("translation-wizard__list")
                vgrow = Priority.ALWAYS
                setCellFactory {
                    LanguageCell(
                        LanguageType.TARGET,
                        viewModel.anglicizedProperty,
                        translationViewModel.existingLanguages
                    ) {
                        translationViewModel.selectedTargetLanguageProperty.set(it)
                    }
                }
                viewModel.searchQueryProperty.onChange {
                    it?.let { if (it.isNotBlank()) scrollTo(0) }
                }
            }
        }
    }

    init {
        importStylesheet(resources.get("/css/translation-wizard.css"))
        importStylesheet(resources.get("/css/language-card-cell.css"))
        importStylesheet(resources.get("/css/filtered-search-bar.css"))
        importStylesheet(resources.get("/css/confirm-dialog.css"))

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
