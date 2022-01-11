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
package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.media.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class AudioPlayerSkin(private val playerNode: AudioPlayerNode) : SkinBase<AudioPlayerNode>(playerNode) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var sourceMissing: HBox
    @FXML
    lateinit var audioPlayer: HBox
    @FXML
    lateinit var audioSlider: Slider

    lateinit var audioController: AudioPlayerController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        audioController = AudioPlayerController(audioSlider)
        playBtn.setOnMouseClicked {
            audioController.toggle()
        }
        audioController.isPlayingProperty.onChangeAndDoNow {
            if (it == true) {
                playBtn.graphicProperty().set(PAUSE_ICON)
            } else {
                playBtn.graphicProperty().set(PLAY_ICON)
            }
        }
        playerNode.audioPlayerProperty.onChange { player ->
            player?.let {
                audioController.load(it)
            }
        }
        if (playerNode.sourceAvailable) {
            sourceMissing.visibleProperty().set(false)
            audioPlayer.visibleProperty().set(true)
        } else {
            sourceMissing.visibleProperty().set(true)
            audioPlayer.visibleProperty().set(false)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
