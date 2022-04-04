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
import javafx.geometry.NodeOrientation
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.PlaybackRateChangedEvent
import org.wycliffeassociates.otter.jvm.controls.media.PlaybackRateType
import org.wycliffeassociates.otter.jvm.controls.media.SimpleAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val minimizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE)
    private val maximizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE)

    @FXML
    lateinit var sourceAudioContainer: HBox

    @FXML
    lateinit var playSourceBtn: Button

    @FXML
    lateinit var playTargetBtn: Button

    @FXML
    lateinit var sourcePlayer: SimpleAudioPlayer

    @FXML
    lateinit var targetPlayer: SimpleAudioPlayer

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
    lateinit var sourceTextChunksContainer: ListView<Label>

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

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        initAudioControls()
        initTextControls()
    }

    private fun initAudioControls() {
        sourceAudioContainer.apply {
            visibleWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
        }

        sourceAudioNotAvailable.apply {
            hiddenWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
        }

        targetAudio.apply {
            visibleWhen(sourceContent.targetAudioPlayerProperty.isNotNull)
            managedWhen(visibleProperty())
            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
        }

        audioNotAvailableText.apply {
            textProperty().bind(sourceContent.audioNotAvailableTextProperty)
        }

        sourcePlayer.apply {
            playerProperty.bind(sourceContent.sourceAudioPlayerProperty)
            enablePlaybackRateProperty.set(true)
            playButtonProperty.set(playSourceBtn)
            playTextProperty.bind(sourceContent.playSourceLabelProperty)
            pauseTextProperty.bind(sourceContent.pauseSourceLabelProperty)
            menuSideProperty.set(Side.TOP)

            sourceContent.sourceSpeedRateProperty.onChangeAndDoNow { rate ->
                audioPlaybackRateProperty.set(rate?.toDouble() ?: 1.0)
            }
            audioPlaybackRateProperty.onChange { rate ->
                if (rate > 0) FX.eventbus.fire(PlaybackRateChangedEvent(PlaybackRateType.SOURCE, rate))
            }
        }

        targetPlayer.apply {
            playerProperty.bind(sourceContent.targetAudioPlayerProperty)
            enablePlaybackRateProperty.set(true)
            playButtonProperty.set(playTargetBtn)
            playTextProperty.bind(sourceContent.playTargetLabelProperty)
            pauseTextProperty.bind(sourceContent.pauseTargetLabelProperty)
            menuSideProperty.set(Side.TOP)

            sourceContent.targetSpeedRateProperty.onChangeAndDoNow { rate ->
                audioPlaybackRateProperty.set(rate?.toDouble() ?: 1.0)
            }

            audioPlaybackRateProperty.onChange { rate ->
                if (rate > 0) FX.eventbus.fire(PlaybackRateChangedEvent(PlaybackRateType.TARGET, rate))
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

        sourceContent.sourceTextChunks.onChangeAndDoNow {
            val textNodes = it.mapIndexed { index, chunkText ->
                buildChunkText(chunkText, index)
            }.toMutableList()
            textNodes.add(buildLicenseText())
            sourceTextChunksContainer.items.setAll(textNodes)
        }

        title.apply {
            textProperty().bind(sourceContent.contentTitleProperty)
        }

        minimizeBtn.apply {
            visibleWhen(sourceContent.isMinimizableProperty)

            setOnAction {
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

    private fun buildChunkText(textContent: String, index: Int): Label {
        return Label(textContent).apply {
            addClass("source-content__text")
            isWrapText = true
            prefWidthProperty().bind(
                sourceTextChunksContainer.widthProperty().minus(50)
            )

            sourceContent.highlightedChunk.onChangeAndDoNow { highlightedIndex ->
                val isHighlighted = highlightedIndex == index
                toggleClass("source-content__text--highlighted", isHighlighted)
                if (isHighlighted) {
                    sourceTextChunksContainer.scrollTo(index)
                }
            }
        }
    }

    private fun buildLicenseText(): Label {
        return Label().apply {
            addClass("source-content__license-text")
            textProperty().bind(sourceContent.licenseTextProperty)
            styleProperty().bind(sourceContent.orientationProperty.objectBinding {
                when (it) {
                    NodeOrientation.LEFT_TO_RIGHT -> "-fx-font-style: italic;"
                    else -> ""
                }
            })
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
