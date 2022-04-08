/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.animation.FadeTransition
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.media.SimpleAudioPlayer
import tornadofx.*
import tornadofx.FX.Companion.messages

class ScriptureTakeCardSkin(val card: ScriptureTakeCard) : SkinBase<ScriptureTakeCard>(card) {

    @FXML
    lateinit var selectBtn: Button

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var editBtn: Button

    @FXML
    lateinit var deleteBtn: Button

    @FXML
    lateinit var takeLabel: Label

    @FXML
    lateinit var timestampLabel: Label

    @FXML
    lateinit var player: SimpleAudioPlayer

    private val selectedIcon = FontIcon(MaterialDesign.MDI_CHECK)
    private val promoteIcon = FontIcon(MaterialDesign.MDI_ARROW_UP)

    init {
        loadFXML()
        initializeControl()
    }

    fun initializeControl() {
        bindText()
        initController()
    }

    fun bindText() {
        takeLabel.textProperty().bind(card.takeLabelProperty)
        timestampLabel.textProperty().bind(card.lastModifiedProperty)
    }

    private fun initController() {
        selectBtn.apply {
            tooltip(messages["select"])
            graphicProperty().bind(card.selectedProperty.objectBinding {
                when (it) {
                    true -> {
                        togglePseudoClass("selected", true)
                        selectedIcon
                    }
                    else -> {
                        togglePseudoClass("selected", false)
                        promoteIcon
                    }
                }
            })
            focusTraversableProperty().bind(card.selectedProperty.not())
            setOnAction {
                card.animationMediatorProperty.value?.let {
                    if (it.isAnimating || card.selectedProperty.value) {
                        return@setOnAction
                    }
                    it.node = card
                    it.animate {
                        card.onTakeSelectedActionProperty.value?.handle(ActionEvent())
                    }
                } ?: card.onTakeSelectedActionProperty.value?.handle(ActionEvent())
            }
        }

        deleteBtn.tooltip(messages["delete"])
        deleteBtn.onActionProperty().bind(card.onTakeDeleteActionProperty)
        card.deletedProperty.onChangeOnce { deleteRequested ->
            if (deleteRequested == true) {
                fade(card) {
                    card.deletedProperty.set(false)
                }
            }
        }

        editBtn.tooltip(messages["edit"])
        editBtn.onActionProperty().bind(card.onTakeEditActionProperty)

        player.apply {
            playerProperty.bind(card.audioPlayerProperty)
            playButtonProperty.set(playBtn)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ScriptureTakeCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }

    private fun fade(node: Node, callback: () -> Unit) {
        val ft = FadeTransition(Duration.millis(600.0), node)
        ft.fromValue = node.opacity
        ft.toValue = 0.0
        ft.onFinished = EventHandler {
            callback()
        }
        ft.play()
    }
}
