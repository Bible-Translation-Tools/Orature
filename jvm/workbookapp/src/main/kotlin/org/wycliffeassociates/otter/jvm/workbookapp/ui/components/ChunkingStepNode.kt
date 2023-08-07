package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*
import tornadofx.FX.Companion.messages

class ChunkingStepNode(
    step: ChunkingStep,
    selectedStepProperty: ObjectProperty<ChunkingStep>,
    reachableStepProperty: ObjectProperty<ChunkingStep>,
    showAllProperty: BooleanProperty,
    content: Node? = null
) : VBox() {
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)

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
        disableWhen {
            reachableStepProperty.booleanBinding {
                it?.let { reachable ->
                    reachable.ordinal < step.ordinal
                } ?: true
            }
        }

        hbox {
            addClass("chunking-step__header-section")
            label(messages[step.titleKey]) {
                addClass("chunking-step__title", "normal-text")
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