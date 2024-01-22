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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.controls.event.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.*
import tornadofx.FX.Companion.messages

class ChunkingStepNode(
    step: ChunkingStep,
    selectedStepProperty: ObjectProperty<ChunkingStep>,
    reachableStepProperty: ObjectProperty<ChunkingStep>,
    isCollapsedProperty: BooleanProperty
) : VBox() {

    val isSelectedProperty = selectedStepProperty.booleanBinding {
        if (it == null) {
            removePseudoClass("selected")
            return@booleanBinding false
        }
        togglePseudoClass("selected",  step == selectedStepProperty.value)
        step == selectedStepProperty.value
    }
    val chunkListProperty: ListProperty<ChunkViewData> = SimpleListProperty()

    private val contentSectionProperty = SimpleObjectProperty<Node>().apply {
        bind(
            chunkListProperty.objectBinding { chunkList: List<ChunkViewData> ->
                ChunkGrid(chunkList)
            }
        )
    }
    private val unavailableProperty = reachableStepProperty.booleanBinding {
        it?.let { reachable ->
            reachable.ordinal < step.ordinal
        } ?: true
    }
    private val completedProperty = selectedStepProperty.booleanBinding {
        if (it == null) {
            return@booleanBinding false
        }
        step.ordinal < selectedStepProperty.value.ordinal
    }

    init {
        addClass("chunking-step")
        focusTraversableProperty().bind(isSelectedProperty.not())
        disableWhen(unavailableProperty)
        completedProperty.onChangeAndDoNow { toggleClass("completed", it == true) }

        stackpane {
            hbox {
                addClass("chunking-step__header-section")
                visibleWhen { isCollapsedProperty.not() }
                managedWhen(visibleProperty())

                label(messages[step.titleKey]) {
                    addClass("chunking-step__title", "h4", "h4--80")
                    graphicProperty().bind(createGraphicBinding(step))
                }
                region { hgrow = Priority.ALWAYS }
            }
            hbox {
                addClass("chunking-step__header-section")
                label {
                    addClass("chunking-step__title")
                    graphicProperty().bind(createGraphicBinding(step))
                }
                visibleWhen { isCollapsedProperty }
                managedWhen(visibleProperty())
            }
        }

        hbox {
            /* expands when step is selected (similar to titled pane & accordion) */
            addClass("chunking-step__content-section")
            bindSingleChild(contentSectionProperty)

            visibleWhen {
                isSelectedProperty.and(isCollapsedProperty.not())
            }
            managedWhen {
                visibleProperty().and(chunkListProperty.emptyProperty().not())
            }
        }

        setOnMouseClicked {
            if (!isSelectedProperty.value) {
                FX.eventbus.fire(ChunkingStepSelectedEvent(step))
                requestFocus()
            }
        }

        this.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (
                (it.code == KeyCode.ENTER || it.code == KeyCode.SPACE) && !isSelectedProperty.value
            ) {
                FX.eventbus.fire(ChunkingStepSelectedEvent(step))
            }
        }
    }

    private fun createGraphicBinding(step: ChunkingStep) : ObjectBinding<Node?> {
        return objectBinding(unavailableProperty, isSelectedProperty, completedProperty) {
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
        ChunkingStep.FINAL_REVIEW -> FontIcon(MaterialDesign.MDI_PLAY).apply { addClass("icon") }
        else -> null
    }
}

fun EventTarget.chunkingStep(
    step: ChunkingStep,
    selectedStepProperty: ObjectProperty<ChunkingStep>,
    reachableStepProperty: ObjectProperty<ChunkingStep>,
    isCollapsedProperty: BooleanProperty,
    op: ChunkingStepNode.() -> Unit = {}
) = ChunkingStepNode(step, selectedStepProperty, reachableStepProperty, isCollapsedProperty).attachTo(this, op)