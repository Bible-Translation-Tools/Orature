package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.FX.Companion.messages
import tornadofx.*

class ChunkingStepsPane : VBox() {
    val chunkItems = observableListOf<ChunkViewData>()
    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val isCollapsedProperty = SimpleBooleanProperty(false)

    init {
        hbox {
            addClass("chunking-step__header-section")
            label("Steps") {
                addClass("h3")
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
                    this@ChunkingStepsPane.maxWidth = if (isCollapsedProperty.value) {
                        320.0
                    } else {
                        80.0
                    }
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
                chunkingStep(ChunkingStep.PEER_EDIT, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkItems.onChange { chunkListProperty.set(it.list) }
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.PEER_EDIT)
                    }
                }
                chunkingStep(ChunkingStep.KEYWORD_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkItems.onChange { chunkListProperty.set(it.list) }
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.KEYWORD_CHECK)
                    }
                }
                chunkingStep(ChunkingStep.VERSE_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    chunkItems.onChange { chunkListProperty.set(it.list) }
                    setOnSelect {
                        selectedStepProperty.set(ChunkingStep.VERSE_CHECK)
                    }
                }
            }
        }

    }
}

fun EventTarget.chunkingStepsPane(op: ChunkingStepsPane.() -> Unit = {}) = ChunkingStepsPane().attachTo(this, op)