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

class ChunkItem : VBox() {
    val chunkTitleProperty = SimpleStringProperty()
    val showTakesProperty = SimpleBooleanProperty(false)
    val hasSelectedProperty = SimpleBooleanProperty(false)

    val takes = observableListOf<TakeModel>()
    val takeViews = observableListOf<TakeItem>()
    private lateinit var takesListView: ListView<TakeItem>
    private var isAnimating = false

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
                            takesListView.selectionModel.select(this)
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

                listview(takes) {
//                    takesListView = this
                    setCellFactory {
                        TakeCell {
                            onTakeSelectedActionProperty.value?.handle(
                                ActionEvent(it, null)
                            )
                        }
                    }

//                    onMouseClicked = EventHandler {
//                        val index = this.selectionModel.selectedIndex
//                        if (isAnimating || selectedItem == null || index <= 0) {
//                            return@EventHandler
//                        }
//                        isAnimating = true
//
//                        val selectedItem = this.selectedItem
//                        selectedItem?.styleClass?.add("selected")
//                        takeViews.forEach {
//                            if (takeViews.indexOf(it) < index) moveDown(it as Node) { }
//                        }
//
//                        moveToTop(selectedItem as Node) {
//                            takeViews.removeAt(index)
//                            takeViews.add(0, selectedItem)
//                            this.selectionModel.select(0)
//                            selectedItem?.styleClass?.remove("selected")
//                        }
//                    }

                    prefHeightProperty().bind(Bindings.size(takes).multiply(TAKE_CELL_HEIGHT))
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

    private fun moveToTop(node: Node, onFinish: () -> Unit) {
        node.viewOrder = -1.0
        val parentY = node.parent.layoutY
        val ttUp = TranslateTransition(Duration.millis(600.0), node)
        ttUp.toY = -parentY

        val ttLeft = TranslateTransition(Duration.millis(400.0), node)
        ttLeft.byX = -20.0
        val ttRight = TranslateTransition(Duration.millis(200.0), node)
        ttRight.byX = 20.0

        val ttLR = SequentialTransition().apply {
            children.addAll(ttLeft, ttRight)
        }

        ParallelTransition()
            .apply {
                children.addAll(ttUp, ttLR)
                onFinished = EventHandler {
                    onFinish()
                    revertTransition(node)
                }
            }
            .play()
    }

    private fun moveDown(node: Node, onFinish: () -> Unit) {
        val distance = node.boundsInLocal.height + 5
        val tt = TranslateTransition(Duration.millis(600.0), node)
        tt.byY = distance
        tt.onFinished = EventHandler {
            revertTransition(node)
        }
        tt.play()
    }

    private fun revertTransition(node: Node) {
        node.viewOrder = 0.0
        val distance = node.translateY
        val tt = TranslateTransition(Duration.millis(1.0), node)

        tt.byY = -distance
        tt.onFinished = EventHandler {
            isAnimating = false
        }
        tt.play()
    }
}
