/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
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
    val noSourceAudioProperty = SimpleBooleanProperty(false)

    private val isCollapsedProperty = SimpleBooleanProperty(false)

    init {
        addClass("chunking-step-drawer")
        hbox {
            addClass("chunking-step-drawer__header-section")
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
                chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, isCollapsedProperty) {
                    visibleWhen(noSourceAudioProperty.not())
                    managedWhen(visibleProperty())
                }
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

            runLater { customizeScrollbarSkin() }
        }
    }
}