package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*
import tornadofx.FX.Companion.messages

class ChunkingStepNode(
    step: ChunkingStep,
    private val selectedStepProperty: ObjectProperty<ChunkingStep>,
    reachableStepProperty: ObjectProperty<ChunkingStep>,
    showAllProperty: BooleanProperty,
    content: Node? = null
) : VBox() {
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val unavailableProperty = reachableStepProperty.booleanBinding {
        it?.let { reachable ->
            reachable.ordinal < step.ordinal
        } ?: true
    }
    private val completedProperty = booleanBinding(selectedStepProperty) {
        step.ordinal < selectedStepProperty.value.ordinal
    }

    init {
        addClass("chunking-step")
        isFocusTraversable = true
        visibleWhen {
            showAllProperty.booleanBinding {
                it == true || step.ordinal >= selectedStepProperty.value.ordinal
            }
                .or(disableProperty())

        }
        managedWhen(visibleProperty())
        disableWhen(unavailableProperty)
        completedProperty.onChangeAndDoNow { toggleClass("completed", it == true) }

        hbox {
            addClass("chunking-step__header-section")
            label(messages[step.titleKey]) {
                addClass("chunking-step__title", "normal-text")
                graphicProperty().bind(createGraphicBinding(step))
            }
            region { hgrow = Priority.ALWAYS }
        }

        pane {
            bindSingleChild(mainSectionProperty)

            visibleWhen { selectedStepProperty.isEqualTo(step) }
            managedWhen(visibleProperty())
            mainSectionProperty.bind(
                selectedStepProperty.objectBinding {
                    this@ChunkingStepNode.togglePseudoClass("selected", it == step)
                    if (it == step) {
                        content
                    } else {
                        null
                    }
                }
            )
        }

        setOnMouseClicked {
            selectedStepProperty.set(step)
            requestFocus()
        }

        this.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER || it.code == KeyCode.SPACE) {
                selectedStepProperty.set(step)
            }
        }
    }

    private fun createGraphicBinding(step: ChunkingStep) : ObjectBinding<Node?> {
        return objectBinding(unavailableProperty, selectedStepProperty, completedProperty) {
            when {
                unavailableProperty.value -> FontIcon(MaterialDesign.MDI_LOCK).apply { addClass("icon") }
                completedProperty.value -> FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("complete-icon") }
                else -> getStepperIcon(step)
            }
        }
    }

    private fun getStepperIcon(step: ChunkingStep) = when (step) {
        ChunkingStep.CONSUME_AND_VERBALIZE -> FontIcon(Material.HEARING).apply { addClass("icon") }
        ChunkingStep.CHUNKING -> FontIcon(MaterialDesign.MDI_CONTENT_CUT).apply { addClass("icon") }
        ChunkingStep.BLIND_DRAFT -> FontIcon(MaterialDesign.MDI_HEADSET).apply { addClass("icon") }
        ChunkingStep.PEER_EDIT -> FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE).apply { addClass("icon") }
        ChunkingStep.KEYWORD_CHECK -> FontIcon(Material.BORDER_COLOR).apply { addClass("icon") }
        ChunkingStep.VERSE_CHECK -> FontIcon(Material.MENU_BOOK).apply { addClass("icon") }
        else -> null
    }
}

fun EventTarget.chunkingStep(
    step: ChunkingStep,
    selectedStepProperty: ObjectProperty<ChunkingStep>,
    reachableStepProperty: ObjectProperty<ChunkingStep>,
    hideCompletedProperty: BooleanProperty,
    content: Node? = null,
    op: ChunkingStepNode.() -> Unit = {}
) = ChunkingStepNode(step, selectedStepProperty, reachableStepProperty, hideCompletedProperty, content).attachTo(
    this,
    op
)