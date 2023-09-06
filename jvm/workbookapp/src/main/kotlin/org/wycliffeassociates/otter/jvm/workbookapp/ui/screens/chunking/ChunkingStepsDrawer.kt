package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.FX.Companion.messages
import tornadofx.*

class ChunkingStepsDrawer : VBox() {
    val chunkItems = observableListOf<ChunkViewData>()
    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.CHUNKING)
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
            isFitToWidth = true
            vbox {
                chunkingStep(ChunkingStep.CONSUME_AND_VERBALIZE,selectedStepProperty,reachableStepProperty, isCollapsedProperty) {
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.CONSUME_AND_VERBALIZE)
                    }
                }
                chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.CHUNKING)
                    }
                }
                chunkingStep(ChunkingStep.BLIND_DRAFT, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkItems.onChange { chunkListProperty.set(it.list) }
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.BLIND_DRAFT)
                    }
                }
            }
        }

    }
}

fun EventTarget.chunkingStepsPane(op: ChunkingStepsDrawer.() -> Unit = {}) = ChunkingStepsDrawer().attachTo(this, op)