package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.fragments

import javafx.beans.property.Property
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.searchablelist.SearchableList
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.controls.searchablelist.searchablelist
import tornadofx.*

class SelectLanguage : Fragment() {

    private val logger = LoggerFactory.getLogger(SelectLanguage::class.java)

    private val viewModel: ProjectWizardViewModel by inject()

    private val sourceList = buildSearchableLanguageList(
        viewModel.sourceLanguages,
        viewModel.selectedSourceLanguage,
        messages["sourceLanguageRequired"]
    )

    private val targetList = buildSearchableLanguageList(
        viewModel.targetLanguages,
        viewModel.selectedTargetLanguage,
        messages["targetLanguageRequired"]
    ).apply {
        viewModel.selectedSourceLanguage.onChange {
            refreshSearch(false)
        }
    }

    private fun buildSearchableLanguageList(
        languages: SortedFilteredList<Language>,
        selectedLanguage: Property<Language>,
        errorMessage: String
    ): SearchableList<Language> {
        return searchablelist(
            languages,
            selectedLanguage
        ) {
            addClass(ProjectWizardStyles.searchableList)
            languages.sortedItems.setComparator { lang1, lang2 ->
                var query = searchField.textProperty().value
                when {
                    lang1.slug.startsWith(query, true) -> -1
                    lang2.slug.startsWith(query, true) -> 1
                    lang1.name.startsWith(query, true) -> -1
                    lang2.name.startsWith(query, true) -> 1
                    lang1.anglicizedName.startsWith(query, true) -> -1
                    lang2.anglicizedName.startsWith(query, true) -> 1
                    else -> 0
                }
            }
            listView.cellCache { language ->
                label("${language.name} (${language.slug})")
            }
            searchField.promptText = messages["languageSearchPrompt"]
            autoSelect = true
            viewModel
                .clearLanguages
                .doOnError { e ->
                    logger.error("Error in clear languages", e)
                }
                .subscribe {
                    searchField.clear()
                    listView.selectionModel.clearSelection()
                }
            selectedLanguage.addValidator(searchField) {
                if (it == null) error(errorMessage) else null
            }
            languages.filterWhen(searchField.textProperty(), viewModel::filterLanguages)
        }
    }

    override val complete = viewModel.languagesValid()

    init {
        importStylesheet<ProjectWizardStyles>()
    }

    override val root = hbox {
        paddingAll = 40.0
        addClass(ProjectWizardStyles.selectLanguageRoot)
        vbox {

            addClass(ProjectWizardStyles.languageSearchContainer)
            label(messages["sourceLanguage"], ProjectWizardStyles.sourceLanguageIcon()) {
                addClass(ProjectWizardStyles.sourceLanguageBoxLabel)
            }
            add(sourceList)
        }

        vbox {
            addClass(ProjectWizardStyles.languageSearchContainer)
            label(messages["targetLanguage"], ProjectWizardStyles.targetLanguageIcon()) {
                addClass(ProjectWizardStyles.targetLanguageBoxLabel)
            }
            add(targetList)
        }
    }

    override fun onSave() {
        viewModel.getRootSources()
    }

    override fun onUndock() {
        super.onUndock()
        sourceList.listView.selectionModel.clearSelection()
        targetList.listView.selectionModel.clearSelection()
    }
}
