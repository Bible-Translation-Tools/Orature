package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.animation.TranslateTransition
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.bar.searchBar
import org.wycliffeassociates.otter.jvm.controls.card.translationTypeCard
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.model.StepDirection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.languageTableView
import tornadofx.*
import tornadofx.FX.Companion.messages

private const val TRANSITION_DURATION_SEC = 0.3

class ProjectWizardSection(
    sourceLanguages: ObservableList<Language>,
    targetLanguages: ObservableList<Language>,
    selectedModeProperty: SimpleObjectProperty<ProjectMode>,
    selectedSourceLanguageProperty: SimpleObjectProperty<Language>
) : StackPane() {
    val sourceLanguageSearchQueryProperty =  SimpleStringProperty()
    val targetLanguageSearchQueryProperty = SimpleStringProperty()

    private val steps: List<Node>
    private val currentStepProperty = SimpleIntegerProperty(0)
    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val step1 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon")
                tooltip(messages["goBack"])
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
                    nextStep()
                }
            }
            translationTypeCard("narration", "narrationDesc") {
                setOnSelectAction {
                    selectedModeProperty.set(ProjectMode.NARRATION)
                    nextStep()
                }
            }
            translationTypeCard("dialect", "dialectDesc") {
                addPseudoClass("last")
                setOnSelectAction {
                    selectedModeProperty.set(ProjectMode.DIALECT)
                    nextStep()
                }
            }
        }

        managedWhen(visibleProperty())
    }

    private val step2 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon")
                tooltip(messages["goBack"])
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction {
                    selectedModeProperty.set(null)
                    previousStep()
                }
            }
            label(messages["selectSourceLanguageStep2"]) { addClass("h4") }
            region { hgrow = Priority.ALWAYS }
            searchBar {
                textProperty().bindBidirectional(sourceLanguageSearchQueryProperty)
                promptText = messages["search"]
            }
        }

        languageTableView(sourceLanguages) {
            this@apply.visibleProperty().onChange {
                if (it) customizeScrollbarSkin()
            }
        }
        managedWhen(visibleProperty())
    }

    private val step3 = VBox().apply {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")

            button {
                addClass("btn", "btn--icon")
                tooltip(messages["goBack"])
                graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)

                setOnAction {
                    selectedSourceLanguageProperty.set(null)
                    previousStep()
                }
            }
            label(messages["selectTargetLanguageStep3"]) { addClass("h4") }
            region { hgrow = Priority.ALWAYS }
            searchBar {
                textProperty().bindBidirectional(targetLanguageSearchQueryProperty)
                promptText = messages["search"]
            }
        }

        languageTableView(targetLanguages) {
            this@apply.visibleProperty().onChange {
                if (it) customizeScrollbarSkin()
            }
        }
        managedWhen(visibleProperty())
    }


    init {
        vgrow = Priority.ALWAYS

        add(step1)
        add(step2)
        add(step3)

        steps = listOf(step1, step2, step3)
    }

    fun setOnCancelAction(op: () -> Unit) {
        onCancelActionProperty.set(EventHandler { op() })
    }

    fun onSectionDocked() {
        currentStepProperty.set(0)
        steps.forEachIndexed { index, node ->
            node.isVisible = index == 0 // allow only the first step to be accessible
        }
        runLater {
            step2.translateX = scene.width
            step3.translateX = scene.width
        }
    }

    fun nextStep() {
        val nextStep = currentStepProperty.value + 1
        renderStepTransition(nextStep, StepDirection.FORWARD)
        currentStepProperty.set(nextStep)
    }

    fun previousStep() {
        val currentStep = currentStepProperty.value
        renderStepTransition(currentStep, StepDirection.BACKWARD)
        currentStepProperty.set(currentStep - 1)
    }

    /**
     * Renders the transition animation when switching between wizard steps.
     *
     * After the animation completes, all step nodes that are not the target
     * will be inaccessible by the view to avoid firing keyboard event on
     * detached element(s).
     *
     * @param step the ordinal of the step that needs to animate
     * @param direction specifies the direction the user is navigating to
     */
    private fun renderStepTransition(step: Int, direction: StepDirection) {
        val nodeToAnimate = steps[step]
        val duration = Duration.seconds(TRANSITION_DURATION_SEC)
        /* horizontal translation is automatically aligned with node orientation */
        if (direction == StepDirection.FORWARD) {
            steps[step].isVisible = true

            TranslateTransition(duration, nodeToAnimate).apply {
                toX = 0.0
                setOnFinished {
                    steps.forEachIndexed { index, node ->
                        node.isVisible = index == step // only enable visibility for the target step
                    }
                }
            }.play()
        } else {
            steps[step - 1].isVisible = true

            TranslateTransition(duration, nodeToAnimate).apply {
                toX = scene.width
                setOnFinished {
                    steps.forEachIndexed { index, node ->
                        node.isVisible = index == step - 1 // only enable visibility for the target step
                    }
                }
            }.play()
        }
    }
}