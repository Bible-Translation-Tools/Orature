package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
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
import tornadofx.*
import tornadofx.FX.Companion.messages

class ProjectWizardSection(
    sourceLanguages: ObservableList<Language>,
    targetLanguages: ObservableList<Language>,
    selectedModeProperty: SimpleObjectProperty<ProjectMode>,
    selectedSourceLanguageProperty: SimpleObjectProperty<Language>
) : StackPane() {
    val sourceLanguageSearchQueryProperty =  SimpleStringProperty()
    val targetLanguageSearchQueryProperty = SimpleStringProperty()

    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val step1 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                onActionProperty().bind(onCancelActionProperty)
            }
            label(messages["selectProjectTypeStep1"]) { addClass("h4") }
        }

        vbox {
            addClass("homepage__main-region__body")
            vgrow = Priority.ALWAYS

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

        visibleWhen { selectedModeProperty.isNull }
        managedWhen(visibleProperty())
    }

    private val step2 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction { selectedModeProperty.set(null) }
            }
            label(messages["selectSourceLanguageStep2"]) { addClass("h4") }
            region { hgrow = Priority.ALWAYS }
            searchBar {
                textProperty().bindBidirectional(sourceLanguageSearchQueryProperty)
                promptText = messages["search"]
            }
        }

        languageTableView(sourceLanguages)

        visibleWhen {
            selectedModeProperty.isNotNull
                .and(selectedSourceLanguageProperty.isNull)
        }
        managedWhen(visibleProperty())
    }

    private val step3 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")

            button {
                addClass("btn", "btn--icon")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction { selectedSourceLanguageProperty.set(null) }
            }
            label(messages["selectTargetLanguageStep3"]) { addClass("h4") }
            region { hgrow = Priority.ALWAYS }
            searchBar {
                textProperty().bindBidirectional(targetLanguageSearchQueryProperty)
                promptText = messages["search"]
            }
        }

        languageTableView(targetLanguages)

        visibleWhen { selectedSourceLanguageProperty.isNotNull }
        managedWhen(visibleProperty())
    }


    init {
        vgrow = Priority.ALWAYS

        add(step1)
        add(step2)
        add(step3)
    }

    fun setOnCancelAction(op: () -> Unit) {
        onCancelActionProperty.set(EventHandler { op() })
    }
}