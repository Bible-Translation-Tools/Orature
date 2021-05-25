package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.LanguageSelectionViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel
import tornadofx.*

class SourceLanguageSelection : Fragment() {
    private val translationViewModel: TranslationViewModel by inject()
    private val viewModel = LanguageSelectionViewModel(translationViewModel.sourceLanguages)
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            translationViewModel.selectedSourceLanguageProperty.stringBinding {
                it?.name ?: messages["sourceLanguage"]
            }
        )
        iconProperty.set(FontIcon(Material.HEARING))
        onClickAction {
            navigator.back()
        }
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("translation-wizard__root")

            label(messages["pickSourceLanguage"]) {
                addClass("translation-wizard__title")
            }
            add(
                FilteredSearchBar().apply {
                    leftIconProperty.set(FontIcon(Material.HEARING))
                    promptTextProperty.set(messages["search"])
                    filterItems.bind(viewModel.menuItems) { it }
                    viewModel.searchQueryProperty.bindBidirectional(textProperty)
                }
            )
            listview(viewModel.filteredLanguages) {
                addClass("translation-wizard__list")
                vgrow = Priority.ALWAYS
                setCellFactory {
                    LanguageCell(LanguageType.SOURCE, viewModel.anglicizedProperty) {
                        translationViewModel.selectedSourceLanguageProperty.set(it)
                    }
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/translation-wizard.css").toExternalForm())

        translationViewModel.sourceLanguages.onChange {
            viewModel.regions.setAll(
                it.list
                    .distinctBy { language -> language.region }
                    .map { language -> language.region }
                    .filter { region -> region.isNotBlank() }
            )
            viewModel.setFilterMenu()
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.resetFilter()
        translationViewModel.reset()
        translationViewModel.loadSourceLanguages()
    }
}
