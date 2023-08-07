package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*

class ChunkingStepNode(
    step: ChunkingStep,
    selectedStepProperty: SimpleObjectProperty<ChunkingStep>,
    reachableStepProperty: SimpleObjectProperty<ChunkingStep>,
    content: Node? = null
) : VBox() {
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)

    init {
        addClass("chunking-step")
        isFocusTraversable = true
        disableWhen {
            reachableStepProperty.booleanBinding {
                it?.let { reachable ->
                    reachable.ordinal < step.ordinal
                } ?: true
            }
        }

        hbox {
            addClass("chunking-step__header-section")
            label(step.name) {
                addClass("chunking-step__title", "normal-text")
                graphic = when (step) {
                    ChunkingStep.CONSUME -> FontIcon(Material.HEARING)
                    ChunkingStep.CHUNKING -> FontIcon(MaterialDesign.MDI_CONTENT_CUT)
                    ChunkingStep.BLIND_DRAFT -> FontIcon(MaterialDesign.MDI_HEADSET)
                    ChunkingStep.PEER_EDIT -> FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE)
                    ChunkingStep.KEYWORD_CHECK -> FontIcon(Material.BORDER_COLOR)
                    ChunkingStep.VERSE_CHECK -> FontIcon(Material.MENU_BOOK)
                    else -> null
                }
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
    selectedStep: SimpleObjectProperty<ChunkingStep>,
    reachableStep: SimpleObjectProperty<ChunkingStep>,
    content: Node? = null,
    op: ChunkingStepNode.() -> Unit = {}
) = ChunkingStepNode(step, selectedStep, reachableStep, content).attachTo(this, op)