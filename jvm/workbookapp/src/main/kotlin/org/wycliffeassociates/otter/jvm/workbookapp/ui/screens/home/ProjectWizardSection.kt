package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.bar.searchBar
import org.wycliffeassociates.otter.jvm.controls.card.translationTypeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.languageTableView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel
import tornadofx.*
import tornadofx.FX.Companion.messages

class ProjectWizardSection(
    sourceLanguages: ObservableList<Language>,
    targetLanguages: ObservableList<Language>,
    selectedModeProperty: SimpleObjectProperty<ProjectMode>,
    selectedSourceLanguageProperty: SimpleObjectProperty<Language>,
) : StackPane() {

    private val step1 = VBox().apply {
        visibleWhen { selectedModeProperty.isNull }
        managedWhen(visibleProperty())

        translationTypeCard("oralTranslation", "oralTranslationDesc") {
            setOnSelectAction {
                selectedModeProperty.set(ProjectMode.TRANSLATION)
            }
        }
        translationTypeCard("narration", "narrationDesc") {
            setOnSelectAction {
                selectedModeProperty.set(ProjectMode.NARRATION)
            }
        }
        translationTypeCard("dialect", "dialectDesc") {
            addPseudoClass("last")
            setOnSelectAction {
                selectedModeProperty.set(ProjectMode.DIALECT)
            }
        }
    }

    private val step2 = VBox().apply {
        visibleWhen {
            selectedModeProperty.isNotNull
                .and(selectedSourceLanguageProperty.isNull)
        }
        managedWhen(visibleProperty())

        hbox {
            button {
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction { selectedModeProperty.set(null) }
            }
            label(messages["pickASourceLanguage"])
            region { hgrow = Priority.ALWAYS }
            searchBar {  }
        }
        languageTableView(sourceLanguages)
    }

    private val step3 = VBox().apply {
        visibleWhen { selectedSourceLanguageProperty.isNotNull }
        managedWhen(visibleProperty())

        hbox {
            button {
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction { selectedSourceLanguageProperty.set(null) }
            }
            label(messages["pickATargetLanguage"])
            region { hgrow = Priority.ALWAYS }
            searchBar {  }
        }
        languageTableView(targetLanguages)
    }


    init {
        addClass("translation-wizard__main")
        vgrow = Priority.ALWAYS

        add(step1)
        add(step2)
        add(step3)
    }
}