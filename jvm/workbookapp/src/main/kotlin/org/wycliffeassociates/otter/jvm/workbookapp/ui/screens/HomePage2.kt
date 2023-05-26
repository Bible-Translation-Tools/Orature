package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookTableSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import tornadofx.*

class HomePage2 : View() {

    private val viewModel: HomePageViewModel2 by inject()
    private val wizardViewModel: ProjectWizardViewModel by inject()

    private val mainComponentProperty = SimpleObjectProperty<Node>(null)
    private val selectedProjectGroupProperty = SimpleObjectProperty<TranslationCard2>(null)

    private val bookFragment = BookTableSection(viewModel.books)
    private val wizardFragment: Node by lazy {
        ProjectWizardSection(
            wizardViewModel.sourceLanguages,
            wizardViewModel.targetLanguages,
            wizardViewModel.selectedModeProperty,
            wizardViewModel.selectedSourceLanguageProperty,
        )
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
                viewModel.loadTranslations()
                mainComponentProperty.set(bookFragment)
            }
        }
    }

    override val root = borderpane {
        centerProperty().bind(mainComponentProperty)
        left = vbox {
            stackpane {
                translationCreationCard {
                    visibleWhen { mainComponentProperty.isNotEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())
                    setOnAction {
                        selectedProjectGroupProperty.set(null)
                        mainComponentProperty.set(wizardFragment)
                    }
                }
                newTranslationCard(
                    wizardViewModel.selectedSourceLanguageProperty,
                    wizardViewModel.selectedModeProperty
                ) {
                    visibleWhen { mainComponentProperty.isEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())

                    setOnCancelAction {
                        exitWizard()
                    }
                }
            }

            vbox { /* list of project groups */
                bindChildren(viewModel.translationModels2) { cardModel ->
                    TranslationCard2(
                        cardModel.sourceLanguage,
                        cardModel.targetLanguage,
                        cardModel.mode,
                        selectedProjectGroupProperty
                    ).apply {

                        setOnAction {
                            if (mainComponentProperty.value !is BookTableSection) {
                                exitWizard()
                            }
                            viewModel.books.setAll(cardModel.books)
                            selectedProjectGroupProperty.set(this)
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.loadTranslations()
        mainComponentProperty.set(bookFragment)
    }

    private fun exitWizard() {
        wizardViewModel.resetWizard()
        mainComponentProperty.set(bookFragment)
    }
}
