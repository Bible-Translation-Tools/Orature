package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookTableSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.EmptyHomeSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import tornadofx.*

class HomePage2 : View() {

    private val viewModel: HomePageViewModel2 by inject()
    private val wizardViewModel: ProjectWizardViewModel by inject()

    private val mainRegionProperty = SimpleObjectProperty<Node>(null)
    private val selectedProjectGroupProperty = SimpleObjectProperty<TranslationCard2>(null)

    private val bookFragment = BookTableSection(viewModel.allBooks)
    private val emptyHomeSection = EmptyHomeSection()
    private val wizardFragment: ProjectWizardSection by lazy {
        ProjectWizardSection(
            wizardViewModel.sourceLanguages,
            wizardViewModel.targetLanguages,
            wizardViewModel.selectedModeProperty,
            wizardViewModel.selectedSourceLanguageProperty,
        ).apply {
            setOnCancelAction { exitWizard() }
        }
    }

    init {
        tryImportStylesheet("/css/control.css")
        tryImportStylesheet("/css/home-page.css")
        tryImportStylesheet("/css/translation-card-2.css")
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")

        subscribe<LanguageSelectedEvent> {
            wizardViewModel.onLanguageSelected(it.item) {
                viewModel.loadProjects().subscribe()
                mainRegionProperty.set(bookFragment)
            }
        }
    }

    override val root = borderpane {
        center = stackpane {
            bindSingleChild(mainRegionProperty)
        }
        left = vbox {
            addClass("homepage__left-pane")
            label(messages["projects"]) {
                addClass("h3", "h3--80", "homepage__left-header")
            }
            stackpane {
                translationCreationCard {
                    visibleWhen { mainRegionProperty.isNotEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())
                    setOnAction {
                        selectedProjectGroupProperty.set(null)
                        mainRegionProperty.set(wizardFragment)
                    }
                }
                newTranslationCard(
                    wizardViewModel.selectedSourceLanguageProperty,
                    wizardViewModel.selectedTargetLanguageProperty,
                    wizardViewModel.selectedModeProperty
                ) {
                    visibleWhen { mainRegionProperty.isEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())

                    setOnCancelAction {
                        exitWizard()
                    }
                }
            }

            vbox { /* list of project groups */
                addClass("homepage__left-pane__project-groups")
                bindChildren(viewModel.translationModels2) { cardModel ->
                    TranslationCard2(
                        cardModel.sourceLanguage,
                        cardModel.targetLanguage,
                        cardModel.mode,
                        selectedProjectGroupProperty
                    ).apply {

                        setOnAction {
                            if (mainRegionProperty.value !is BookTableSection) {
                                exitWizard()
                            }
                            viewModel.allBooks.setAll(cardModel.books)
                            selectedProjectGroupProperty.set(this)
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.loadProjects()
            .subscribe { list ->
                if (list.isEmpty()) {
                    mainRegionProperty.set(emptyHomeSection)
                } else {
                    mainRegionProperty.set(bookFragment)
                }
            }
    }

    private fun exitWizard() {
        wizardViewModel.resetWizard()
        mainRegionProperty.set(bookFragment)
    }
}
