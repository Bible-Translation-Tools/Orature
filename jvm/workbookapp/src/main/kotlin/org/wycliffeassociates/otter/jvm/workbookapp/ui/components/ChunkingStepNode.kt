package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.Node
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
    currentStepProperty: SimpleObjectProperty<ChunkingStep>,
    content: Node? = null
) : VBox() {
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)

    init {
        addClass("chunking-step")

        hbox {
            addClass("chunking-step__title-section")
            label(step.name) {
                addClass("chunking-step__title", "normal-text")
                graphic = when(step) {
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

            visibleWhen { currentStepProperty.isEqualTo(step) }
            managedWhen(visibleProperty())
            mainSectionProperty.bind(
                currentStepProperty.objectBinding { if (it == step) content else null }
            )
        }

        setOnMouseClicked {
            currentStepProperty.set(step)
        }
    }
}

fun EventTarget.chunkingStep(
    step: ChunkingStep,
    currentStep: SimpleObjectProperty<ChunkingStep>,
    content: Node? = null,
    op: ChunkingStepNode.() -> Unit = {}
) = ChunkingStepNode(step, currentStep, content).attachTo(this, op)