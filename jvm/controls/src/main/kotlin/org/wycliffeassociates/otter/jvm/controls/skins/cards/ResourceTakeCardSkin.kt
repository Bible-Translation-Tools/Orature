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
package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.input.ClipboardContent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
import org.wycliffeassociates.otter.jvm.controls.card.ResourceTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.controls.media.SimpleAudioPlayer
import tornadofx.*

class ResourceTakeCardSkin(val card: ResourceTakeCard) : SkinBase<ResourceTakeCard>(card) {

    lateinit var cardNode: Node

    private val dragDropContainer = StackPane()
    private val back = EmptyCardCell()

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var editBtn: Button

    @FXML
    lateinit var deleteBtn: Button

    @FXML
    lateinit var player: SimpleAudioPlayer

    @FXML
    lateinit var takeLabel: Label

    @FXML
    lateinit var takeDrag: Label

    init {
        loadFXML()
        initializeControl()
    }

    fun initializeControl() {
        bindText()
        initController()

        back.apply {
            addClass("card--resource-take--empty")
        }
    }

    fun bindText() {
        takeLabel.textProperty().bind(card.takeNumberProperty())
    }

    fun initController() {
        deleteBtn.setOnAction {
            error(
                FX.messages["deleteTakePrompt"],
                FX.messages["cannotBeUndone"],
                ButtonType.YES,
                ButtonType.NO,
                title = FX.messages["deleteTakePrompt"]
            ) { button: ButtonType ->
                if (button == ButtonType.YES) {
                    skinnable.fireEvent(
                        DeleteTakeEvent(card.takeProperty().value)
                    )
                }
            }
        }
        editBtn.setOnAction {
            skinnable.fireEvent(
                TakeEvent(
                    card.takeProperty().value,
                    {
                        card.audioPlayerProperty().value.load(card.takeProperty().value.file)
                    },
                    TakeEvent.EDIT_TAKE
                )
            )
        }
        card.apply {
            vgrow = Priority.ALWAYS
        }
        cardNode.apply {
            setOnDragDetected {
                startDrag(it)
                card.isDraggingProperty().value = true
                it.consume()
            }
            setOnDragDone {
                card.isDraggingProperty().value = false
                it.consume()
            }
            setOnMouseReleased {
                card.isDraggingProperty().value = false
                it.consume()
            }
            hiddenWhen(card.isDraggingProperty())
        }
        player.apply {
            playerProperty.bind(card.audioPlayerProperty())
            playButtonProperty.set(playBtn)
        }
    }

    private fun startDrag(evt: MouseEvent) {
        val db = cardNode.startDragAndDrop(*TransferMode.ANY)
        val content = ClipboardContent()
        content.putString(card.takeProperty().value.name)
        db.setContent(content)
        val sp = SnapshotParameters()
        sp.fill = Color.TRANSPARENT
        db.dragView = cardNode.snapshot(sp, null)
        evt.consume()
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ResourceTakeCard.fxml"))
        loader.setController(this)
        cardNode = loader.load()

        dragDropContainer.add(back)
        dragDropContainer.add(cardNode)
        children.addAll(dragDropContainer)
    }
}
