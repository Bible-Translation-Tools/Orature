package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.FX.Companion.messages
import tornadofx.*

class ChunkingStepsDrawer(
    selectedStepProperty: SimpleObjectProperty<ChunkingStep>
) : VBox() {
    val chunksProperty = SimpleListProperty<ChunkViewData>()
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)

    private val isCollapsedProperty = SimpleBooleanProperty(false)

    init {
        addClass("chunking-step-drawer")
        hbox {
            addClass("chunking-step__header-section")
            label(messages["steps"]) {
                addClass("h3", "h3--80")
                visibleWhen { isCollapsedProperty.not() }
                managedWhen(visibleProperty())
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon")
                graphicProperty().bind(isCollapsedProperty.objectBinding {
                    if (it == true) {
                        FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT)
                    } else {
                        FontIcon(MaterialDesign.MDI_CHEVRON_LEFT)
                    }
                })
                tooltip {
                    textProperty().bind(isCollapsedProperty.stringBinding {
                        if (it == true) messages["expand"] else messages["collapse"]
                    })
                }
                action {
                    val collapsed = isCollapsedProperty.value
                    this@ChunkingStepsDrawer.togglePseudoClass("collapsed", !collapsed)
                    isCollapsedProperty.set(!isCollapsedProperty.value)
                }
            }

        }
        scrollpane {
            vgrow = Priority.ALWAYS
            isFitToWidth = true
            vbox {
                chunkingStep(ChunkingStep.CONSUME_AND_VERBALIZE,selectedStepProperty,reachableStepProperty, isCollapsedProperty)
                chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, isCollapsedProperty)
                chunkingStep(ChunkingStep.BLIND_DRAFT, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkListProperty.bind(chunksProperty)
                }
                chunkingStep(ChunkingStep.PEER_EDIT, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkListProperty.bind(chunksProperty)
                }
                chunkingStep(ChunkingStep.KEYWORD_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkListProperty.bind(chunksProperty)
                }
                chunkingStep(ChunkingStep.VERSE_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkListProperty.bind(chunksProperty)
                }
                chunkingStep(ChunkingStep.FINAL_REVIEW, selectedStepProperty, reachableStepProperty, isCollapsedProperty)
            }
        }
    }
}