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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*

class TakeItem : HBox() {
    val takeProperty = SimpleObjectProperty<TakeModel>()
    val selectedProperty = SimpleBooleanProperty(false)

    private val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val selectedIcon = FontIcon(MaterialDesign.MDI_CHECK)
    private val promoteIcon = FontIcon(MaterialDesign.MDI_ARROW_UP)

    init {
        styleClass.setAll("take-item")

        simpleaudioplayer {
            hgrow = Priority.ALWAYS

            takeProperty.onChange { take ->
                take?.let {
                    fileProperty.set(take.take.file)
                    playerProperty.set(take.audioPlayer)
                }
            }
        }

        button {
            addClass("btn", "btn--icon")
            graphicProperty().bind(selectedProperty.objectBinding {
                when (it) {
                    true -> selectedIcon
                    else -> promoteIcon
                }
            })
            selectedProperty.onChange {
                togglePseudoClass("selected", it)
            }
            setOnAction {
                if (!isAnimating) {
                    isAnimating = true
                    animate {
                        onTakeSelectedActionProperty.value?.handle(ActionEvent())
                    }
                }
            }
        }
    }

    fun setOnTakeSelected(op: () -> Unit) {
        onTakeSelectedActionProperty.set(EventHandler { op.invoke() })
    }

    private fun animate(callback: () -> Unit) {
        shiftOtherNodes()

        val selectedNode = this
        val parentY = this.parent.layoutY

        // move selected node to top of the list
        val ttUp = TranslateTransition(Duration.millis(600.0), selectedNode)
        ttUp.toY = -parentY

        // arc-like animation
        val ttLeft = TranslateTransition(Duration.millis(400.0), selectedNode)
        ttLeft.byX = -20.0
        val ttRight = TranslateTransition(Duration.millis(200.0), selectedNode)
        ttRight.byX = 20.0

        val ttLR = SequentialTransition().apply {
            children.addAll(ttLeft, ttRight)
        }

        ParallelTransition()
            .apply {
                children.addAll(ttUp, ttLR)
                onFinished = EventHandler {
                    revertAnimation(selectedNode) { isAnimating = false }
                    callback()
                }
            }
            .play()
    }

    private fun shiftOtherNodes() {
        val listView = this.parent.findParent<ListView<TakeItem>>() ?: return
        val selectedIndex = listView.items.indexOf(this)

        for (item in listView.items) {
            if (listView.items.indexOf(item) < selectedIndex) {
                moveDown(item)
            }
        }
    }

    private fun moveDown(node: Node) {
        val distance = node.boundsInLocal.height + 5
        val tt = TranslateTransition(Duration.millis(600.0), node)
        tt.byY = distance
        tt.onFinished = EventHandler {
            revertAnimation(node)
        }
        tt.play()
    }

    private fun revertAnimation(node: Node, onFinish: () -> Unit = { }) {
        val distance = node.translateY
        val ttRevertUp = TranslateTransition(Duration.millis(1.0), node)
        ttRevertUp.byY = -distance
        ttRevertUp.onFinished = EventHandler {
            onFinish()
        }
        ttRevertUp.play()
    }

    companion object {
        private var isAnimating = false
    }
}
