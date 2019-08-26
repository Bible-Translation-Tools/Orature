package org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view.fragments

import javafx.geometry.Insets
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view.ProjectCreatorStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectcreator.viewmodel.ProjectCreatorViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.widgets.searchablelist.searchablelist
import tornadofx.*

class SelectLanguage : Fragment() {
    private val viewModel: ProjectCreatorViewModel by inject()

    override val complete = viewModel.languagesValid()

    init {
        importStylesheet<ProjectCreatorStyles>()
    }

    override val root = hbox {
        padding = Insets(40.0)
        addClass(ProjectWizardStyles.selectLanguageRoot)
        vbox {

            addClass(ProjectWizardStyles.languageSearchContainer)
            label(messages["sourceLanguage"], ProjectCreatorStyles.sourceLanguageIcon()) {
                addClass(ProjectCreatorStyles.sourceLanguageBoxLabel)
            }
            searchablelist(viewModel.filteredLanguages, viewModel.sourceLanguageProperty) {
                addClass(ProjectCreatorStyles.searchableList)
                listView.cellCache { language ->
                    label("${language.name} (${language.slug})")
                }
                searchField.promptText = messages["languageSearchPrompt"]
                autoSelect = true
                filter(viewModel::filterLanguages)
                viewModel.clearLanguages.subscribe {
                    searchField.clear()
                    listView.selectionModel.clearSelection()
                }
                viewModel.sourceLanguageProperty.addValidator(searchField) {
                    if (it == null) error("Source language is required") else null
                }
            }
        }

        vbox {
            addClass(ProjectCreatorStyles.languageSearchContainer)
            label(messages["targetLanguage"], ProjectCreatorStyles.targetLanguageIcon()) {
                addClass(ProjectCreatorStyles.targetLanguageBoxLabel)
            }
            searchablelist(viewModel.allLanguages, viewModel.targetLanguageProperty) {
                addClass(ProjectCreatorStyles.searchableList)
                listView.cellCache { language ->
                    label("${language.name} (${language.slug})")
                }
                searchField.promptText = messages["languageSearchPrompt"]
                autoSelect = true
                viewModel.sourceLanguageProperty.onChange {
                    refreshSearch(false)
                }
                filter(viewModel::filterLanguages)
                viewModel.clearLanguages.subscribe {
                    searchField.clear()
                    listView.selectionModel.clearSelection()
                }
                viewModel.targetLanguageProperty.addValidator(searchField) {
                    if (it == null) error("Target language is required") else null
                }
            }
        }
    }

    override fun onSave() {
        viewModel.getRootSources()
    }
}