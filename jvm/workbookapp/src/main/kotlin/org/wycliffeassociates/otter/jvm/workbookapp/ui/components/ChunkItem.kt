/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import javafx.animation.ParallelTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*

private const val TAKE_CELL_HEIGHT = 60.0
private const val TAKE_CELL_PADDING = 7.0

class ChunkItem : VBox() {
    val chunkTitleProperty = SimpleStringProperty()
    val showTakesProperty = SimpleBooleanProperty(false)
    val hasSelectedProperty = SimpleBooleanProperty(false)

    val takes = observableListOf<TakeModel>()
    val takeViews = observableListOf<TakeItem>()

    private val onChunkOpenActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val downIcon = FontIcon(MaterialDesign.MDI_MENU_DOWN)
    private val upIcon = FontIcon(MaterialDesign.MDI_MENU_UP)
    private val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("chunk-item")

        takes.onChange { model ->
            hasSelectedProperty.set(model.list?.any { it.selected } ?: false)

            takeViews.setAll(
                model.list.map { takeModel ->
                    TakeItem().apply {
                        selectedProperty.set(takeModel.selected)
                        takeProperty.set(takeModel)

                        setOnTakeSelected {
                            onTakeSelectedActionProperty.value?.handle(
                                ActionEvent(takeModel, null)
                            )
                        }
                    }
                }
            )
        }

        hbox {
            vbox {
                hgrow = Priority.ALWAYS
                label {
                    addClass("chunk-item__title")
                    textProperty().bind(chunkTitleProperty)
                }
                label {
                    addClass("chunk-item__take-counter")
                    graphic = FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC)
                    textProperty().bind(takes.sizeProperty.asString())
                }
            }
            hbox {
                addClass("chunk-item__status")
                circle {
                    addClass("chunk-item__selected-status")
                    hasSelectedProperty.onChange {
                        toggleClass("chunk-item__selected-status--active", it)
                    }
                    radius = 12.0
                }
                label {
                    addClass("chunk-item__show-takes")
                    graphicProperty().bind(showTakesProperty.objectBinding {
                        when (it) {
                            true -> upIcon
                            else -> downIcon
                        }
                    })
                }
            }

            setOnMouseClicked {
                showTakesProperty.set(showTakesProperty.value.not())
            }
        }
        vbox {
            addClass("chunk-item__takes")
            visibleWhen(showTakesProperty)
            managedProperty().bind(visibleProperty())

            button {
                addClass("btn", "btn--secondary")
                text = FX.messages["openVerse"]
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                onActionProperty().bind(onChunkOpenActionProperty)
            }

            vbox {
                addClass("chunk-item__take-items")

                listview(takeViews) {
                    prefHeightProperty().bind(
                        Bindings.size(takes).multiply(TAKE_CELL_HEIGHT + TAKE_CELL_PADDING*2)
                    )
                }
            }
        }
    }

    fun setOnChunkOpen(op: () -> Unit) {
        onChunkOpenActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnTakeSelected(op: (take: TakeModel) -> Unit) {
        onTakeSelectedActionProperty.set(
            EventHandler { op.invoke(it.source as TakeModel) }
        )
    }
}
