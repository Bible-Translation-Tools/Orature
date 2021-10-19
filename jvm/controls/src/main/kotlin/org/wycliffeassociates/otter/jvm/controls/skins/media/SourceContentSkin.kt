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

import com.jfoenix.controls.JFXSlider
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.framesToTimecode
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val playIcon = MaterialDesign.MDI_PLAY
    private val pauseIcon = MaterialDesign.MDI_PAUSE

    private val minimizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE)
    private val maximizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE)

    @FXML
    lateinit var sourceAudioContainer: HBox

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var playTargetBtn: Button

    @FXML
    lateinit var audioSlider: JFXSlider

    @FXML
    lateinit var targetAudioSlider: JFXSlider

    @FXML
    lateinit var sourceAudioNotAvailable: HBox

    @FXML
    lateinit var audioNotAvailableText: Label

    @FXML
    lateinit var targetAudio: HBox

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
    lateinit var licenseText: Label

    @FXML
    lateinit var title: Label

    @FXML
    lateinit var titleContainer: HBox

    @FXML
    lateinit var sourceContentBody: VBox

    @FXML
    lateinit var minimizeBtn: Button

    @FXML
    lateinit var sourceAudioBlock: VBox

    lateinit var audioController: AudioPlayerController
    lateinit var targetAudioController: AudioPlayerController

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
        audioSlider.setValueFactory {
            Bindings.createStringBinding(
                {
                    framesToTimecode(it.value, sourceContent.audioSampleRate.value)
                },
                it.valueProperty()
            )
        }
        targetAudioSlider.setValueFactory {
            Bindings.createStringBinding(
                {
                    framesToTimecode(it.value, sourceContent.audioSampleRate.value)
                },
                it.valueProperty()
            )
        }

        audioController = AudioPlayerController(audioSlider)
        targetAudioController = AudioPlayerController(targetAudioSlider)
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

        targetAudio.apply {
            visibleWhen(sourceContent.targetAudioPlayerProperty.isNotNull)
            managedWhen(visibleProperty())
        }

        audioNotAvailableText.apply {
            textProperty().bind(sourceContent.audioNotAvailableTextProperty)
        }

        playBtn.apply {
            tooltip { textProperty().bind(this@apply.textProperty()) }
            setOnMouseClicked {
                audioController.toggle()
            }
        }

        playTargetBtn.apply {
            tooltip { textProperty().bind(this@apply.textProperty()) }
            setOnMouseClicked {
                targetAudioController.toggle()
            }
        }

        audioController.isPlayingProperty.onChangeAndDoNow {
            togglePlayButtonIcon(playBtn, it)
            togglePlayButtonStyle(playBtn, it)
            togglePlayButtonText(it)
        }

        targetAudioController.isPlayingProperty.onChangeAndDoNow {
            togglePlayButtonIcon(playTargetBtn, it)
            togglePlayButtonStyle(playTargetBtn, it)
            toggleTargetPlayButtonText(it)
        }

        sourceContent.audioPlayerProperty.onChangeAndDoNow { player ->
            player?.let {
                audioController.load(it)
            }
        }

        sourceContent.targetAudioPlayerProperty.onChangeAndDoNow { player ->
            player?.let {
                targetAudioController.load(it)
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

        licenseText.apply {
            textProperty().bind(sourceContent.licenseTextProperty)
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

    private fun togglePlayButtonIcon(btn: Button, isPlaying: Boolean?) {
        if (isPlaying == true) {
            (btn.graphic as? FontIcon)?.iconCode = pauseIcon
        } else {
            (btn.graphic as? FontIcon)?.iconCode = playIcon
        }
    }

    private fun togglePlayButtonStyle(btn: Button, isPlaying: Boolean?) {
        if (isPlaying == true) {
            btn.removeClass("btn--primary")
            btn.addClass("btn--secondary")
        } else {
            btn.removeClass("btn--secondary")
            btn.addClass("btn--primary")
        }
    }

    private fun togglePlayButtonText(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.text = sourceContent.pauseLabelProperty.value
        } else {
            playBtn.text = sourceContent.playLabelProperty.value
        }
    }

    private fun toggleTargetPlayButtonText(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playTargetBtn.text = sourceContent.pauseTargetLabelProperty.value
        } else {
            playTargetBtn.text = sourceContent.playTargetLabelProperty.value
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
