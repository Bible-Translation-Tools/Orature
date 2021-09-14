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
package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val playIcon = FontIcon("fa-play")
    private val pauseIcon = FontIcon("fa-pause")

    private val minimizedIcon = FontIcon("mdi-window-minimize")
    private val maximizedIcon = FontIcon("mdi-window-maximize")

    @FXML
    lateinit var sourceAudioContainer: HBox

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var audioSlider: Slider

    @FXML
    lateinit var sourceAudioNotAvailable: HBox

    @FXML
    lateinit var audioNotAvailableText: Label

    @FXML
    lateinit var sourceTextContainer: VBox

    @FXML
    lateinit var sourceTextNotAvailable: HBox

    @FXML
    lateinit var textNotAvailableText: Label

    @FXML
    lateinit var sourceTextScroll: ScrollPane

    @FXML
    lateinit var sourceText: Label

    @FXML
    lateinit var title: Label

    @FXML
    lateinit var titleContainer: HBox

    @FXML
    lateinit var sourceContentBody: VBox

    @FXML
    lateinit var minimizeBtn: Button

    @FXML
    lateinit var sourceAudioBlock: HBox

    lateinit var audioController: AudioPlayerController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        initControllers()
        initAudioControls()
        initTextControls()
    }

    private fun initControllers() {
        audioController = AudioPlayerController(audioSlider)
    }

    private fun initAudioControls() {
        sourceAudioContainer.apply {
            visibleWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
        }

        sourceAudioNotAvailable.apply {
            hiddenWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
        }

        audioNotAvailableText.apply {
            textProperty().bind(sourceContent.audioNotAvailableTextProperty)
        }

        playBtn.apply {
            setOnMouseClicked {
                audioController.toggle()
            }
        }

        audioController.isPlayingProperty.onChangeAndDoNow {
            togglePlayButtonIcon(it)
            togglePlayButtonStyle(it)
            togglePlayButtonText(it)
        }

        sourceContent.audioPlayerProperty.onChangeAndDoNow { player ->
            player?.let {
                audioController.load(it)
            }
        }

        sourceAudioBlock.apply {
            visibleWhen(sourceContent.enableAudioProperty)
            managedWhen(visibleProperty())
        }
    }

    private fun initTextControls() {
        sourceTextNotAvailable.apply {
            hiddenWhen(sourceContent.sourceTextAvailableProperty)
            managedWhen(visibleProperty())
        }

        textNotAvailableText.apply {
            textProperty().bind(sourceContent.textNotAvailableTextProperty)
        }

        sourceTextContainer.apply {
            visibleWhen(sourceContent.sourceTextAvailableProperty)
            managedWhen(visibleProperty())
        }

        sourceTextScroll.apply {
            whenVisible { vvalue = 0.0 }
            isFitToWidth = true
        }

        sourceText.apply {
            textProperty().bind(sourceContent.sourceTextProperty)
        }

        title.apply {
            textProperty().bind(sourceContent.contentTitleProperty)
        }

        minimizeBtn.apply {
            visibleWhen(sourceContent.isMinimizableProperty)

            setOnMouseClicked {
                toggleBody()
            }
            sourceContent.isMinimizedProperty.onChange { isMinimized ->
                minimizeBtn.graphicProperty().value =
                    if (isMinimized) {
                        maximizedIcon
                    } else {
                        minimizedIcon
                    }
            }
        }

        sourceContentBody.apply {
            hiddenWhen(sourceContent.isMinimizedProperty)
            managedWhen(visibleProperty())
        }
    }

    private fun toggleBody() {
        sourceContent.isMinimizedProperty.set(!sourceContent.isMinimizedProperty.value)
    }

    private fun togglePlayButtonIcon(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.graphicProperty().set(pauseIcon)
        } else {
            playBtn.graphicProperty().set(playIcon)
        }
    }

    private fun togglePlayButtonStyle(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.removeClass("btn--primary")
            playBtn.addClass("btn--secondary")
        } else {
            playBtn.removeClass("btn--secondary")
            playBtn.addClass("btn--primary")
        }
    }

    private fun togglePlayButtonText(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.text = sourceContent.pauseLabelProperty.value
        } else {
            playBtn.text = sourceContent.playLabelProperty.value
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
