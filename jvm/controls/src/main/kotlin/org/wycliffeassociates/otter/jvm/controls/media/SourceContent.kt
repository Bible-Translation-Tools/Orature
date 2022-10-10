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
package org.wycliffeassociates.otter.jvm.controls.media

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Popup
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.*
import tornadofx.*
import java.text.MessageFormat

class SourceTextZoomRateChangedEvent(val rate: Int) : FXEvent()

private val minimizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE)
private val maximizedIcon = FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE)

class SourceContent : StackPane() {
    val contentTitleProperty = SimpleStringProperty()

    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = sourceAudioPlayerProperty.isNotNull
    val sourceSpeedRateProperty = SimpleDoubleProperty()
    val targetAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val targetSpeedRateProperty = SimpleDoubleProperty()

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextAvailableProperty: BooleanBinding = sourceTextProperty.isNotNull
    val sourceTextCompactMode = SimpleBooleanProperty(false)
    val sourceTextChunks = observableListOf<String>()
    val highlightedChunk = SimpleIntegerProperty(-1)

    val licenseProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()

    val audioNotAvailableTextProperty = SimpleStringProperty()
    val textNotAvailableTextProperty = SimpleStringProperty()

    val playSourceLabelProperty = SimpleStringProperty()
    val pauseSourceLabelProperty = SimpleStringProperty()

    val playTargetLabelProperty = SimpleStringProperty()
    val pauseTargetLabelProperty = SimpleStringProperty()

    val enableAudioProperty = SimpleBooleanProperty(true)
    val isMinimizableProperty = SimpleBooleanProperty(true)
    val isMinimizedProperty = SimpleBooleanProperty(false)
    val zoomRateProperty = SimpleIntegerProperty(100)

    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val sourceOrientationProperty = SimpleObjectProperty<NodeOrientation>()

    val listeners = mutableListOf<ListenerDisposer>()

    private lateinit var minimizeBtn: Button
    private lateinit var sourceTextChunksContainer: ListView<Label>
    private lateinit var playTargetBtn: Button
    private lateinit var targetPlayer: SimpleAudioPlayer
    private lateinit var playSourceBtn: Button
    private lateinit var sourcePlayer: SimpleAudioPlayer

    private val sourceTextPopup: Popup by lazy {
        Popup().apply {
            isAutoHide = true

            val contentNode = buildTextPopupContent()
            content.setAll(contentNode)
        }
    }

    init {
        tryImportStylesheet(javaClass.getResource("/css/source-content.css").toExternalForm())

        addClass("source-content")

        vbox {
            addClass("source-content__root")

            hbox {
                hbox {
                    addClass("source-content__zoom-control")

                    button {
                        addClass("btn", "btn--icon", "btn--borderless")
                        graphic = FontIcon(MaterialDesign.MDI_MAGNIFY_MINUS)

                        action {
                            textZoom(-10)
                        }
                    }

                    label {
                        addClass("source-content__zoom-rate-text")

                        textProperty().bind(zoomRateProperty.stringBinding {
                            String.format("%d%%", it)
                        })
                    }

                    button {
                        addClass("btn", "btn--icon", "btn--borderless")
                        graphic = FontIcon(MaterialDesign.MDI_MAGNIFY_PLUS)

                        action {
                            textZoom(10)
                        }
                    }
                }

                hbox {
                    hgrow = Priority.ALWAYS
                    alignment = Pos.CENTER
                    label {
                        addClass("source-content__title")
                        textProperty().bind(contentTitleProperty)
                    }
                }

                button {
                    minimizeBtn = this
                    addClass("btn", "btn--icon", "source-content__minimize-btn")

                    graphic = FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE)

                    visibleWhen(isMinimizableProperty)

                    setOnAction {
                        toggleBody()
                    }
                }

                visibleWhen(sourceTextCompactMode.not())
                managedWhen(visibleProperty())
            }

            vbox {
                addClass("source-content__body")
                vgrow = Priority.ALWAYS

                vbox {
                    addClass("source-content__top")
                    vgrow = Priority.ALWAYS

                    hbox {
                        addClass("source-content__not-available")
                        isManaged = false
                        isVisible = false

                        label {
                            textProperty().bind(textNotAvailableTextProperty)
                        }

                        hiddenWhen(sourceTextAvailableProperty)
                        managedWhen(visibleProperty())
                    }

                    vbox {
                        addClass("source-content__text-container")
                        vgrow = Priority.ALWAYS

                        listview<Label> {
                            sourceTextChunksContainer = this
                            addClass("wa-list-view", "source-content__chunk-list")
                            vgrow = Priority.ALWAYS
                            enableScrollByKey()
                        }

                        visibleWhen(sourceTextAvailableProperty.and(sourceTextCompactMode.not()))
                        managedWhen(visibleProperty())
                    }
                }

                vbox {
                    addClass("source-content__bottom")

                    hbox {
                        addClass("source-content__audio_container", "source-content__audio_container--target")

                        button {
                            playTargetBtn = this
                            addClass("btn", "btn--primary", "source-content__play-audio-btn")
                            graphic = FontIcon(MaterialDesign.MDI_PLAY)
                        }

                        simpleaudioplayer {
                            targetPlayer = this
                            hgrow = Priority.ALWAYS

                            playerProperty.bind(targetAudioPlayerProperty)
                            enablePlaybackRateProperty.set(true)
                            playButtonProperty.set(playTargetBtn)
                            playTextProperty.bind(playTargetLabelProperty)
                            pauseTextProperty.bind(pauseTargetLabelProperty)
                            menuSideProperty.set(Side.TOP)
                        }

                        // dummy button for better spacing & alignment
                        button {
                            addClass("btn", "btn--icon")
                            isVisible = false
                            managedWhen(sourceTextCompactMode)
                        }

                        visibleWhen(targetAudioPlayerProperty.isNotNull)
                        managedWhen(visibleProperty())
                        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
                    }

                    hbox {
                        addClass("source-content__control-group")

                        hbox {
                            addClass("source-content__not-available")
                            hgrow = Priority.ALWAYS
                            isManaged = false
                            isVisible = false

                            label {
                                textProperty().bind(audioNotAvailableTextProperty)
                            }

                            hiddenWhen(sourceAudioAvailableProperty)
                            managedWhen(visibleProperty())
                        }

                        hbox {
                            addClass("source-content__audio_container")
                            hgrow = Priority.ALWAYS

                            button {
                                playSourceBtn = this
                                addClass("btn", "btn--primary", "source-content__play-audio-btn")
                                graphic = FontIcon(MaterialDesign.MDI_PLAY)
                            }

                            simpleaudioplayer {
                                sourcePlayer = this
                                hgrow = Priority.ALWAYS

                                playerProperty.bind(sourceAudioPlayerProperty)
                                enablePlaybackRateProperty.set(true)
                                playButtonProperty.set(playSourceBtn)
                                playTextProperty.bind(playSourceLabelProperty)
                                pauseTextProperty.bind(pauseSourceLabelProperty)
                                menuSideProperty.set(Side.TOP)
                            }

                            visibleWhen(sourceAudioAvailableProperty)
                            managedWhen(visibleProperty())
                            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
                        }

                        button {
                            addClass("btn", "btn--icon")
                            graphic = FontIcon(MaterialDesign.MDI_FILE_DOCUMENT)

                            visibleWhen(sourceTextCompactMode)
                            managedWhen(visibleProperty())

                            action {
                                setPopupTheme(sourceTextPopup)
                                val bound = this.boundsInLocal
                                val screenBound = this.localToScreen(bound)
                                sourceTextPopup.show(
                                    FX.primaryStage
                                )
                                sourceTextPopup.x = screenBound.centerX - sourceTextPopup.width + this.width
                                sourceTextPopup.y = screenBound.minY - sourceTextPopup.height
                            }
                        }
                    }

                    visibleWhen(enableAudioProperty)
                    managedWhen(visibleProperty())
                }

                hiddenWhen(isMinimizedProperty)
                managedWhen(visibleProperty())
            }
        }

        bindListFocusableWithScrollable()
        initializeListeners()
    }

    fun initializeListeners() {
        removeListeners()
        initializeAudioListeners()
        initializeSourceContentListeners()
    }

    private fun initializeAudioListeners() {
        sourcePlayer.audioPlaybackRateProperty.onChangeWithDisposer { rate ->
            rate?.let {
                if (rate.toDouble() > 0) {
                    FX.eventbus.fire(PlaybackRateChangedEvent(PlaybackRateType.SOURCE, rate.toDouble()))
                }
            }
        }.let(listeners::add)
        sourceSpeedRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourcePlayer.audioPlaybackRateProperty.set(rate?.toDouble() ?: 1.0)
        }.let(listeners::add)

        targetPlayer.audioPlaybackRateProperty.onChangeWithDisposer { rate ->
            rate?.let {
                if (rate.toDouble() > 0) {
                    FX.eventbus.fire(PlaybackRateChangedEvent(PlaybackRateType.TARGET, rate.toDouble()))
                }
            }
        }.let(listeners::add)
        targetSpeedRateProperty.onChangeAndDoNowWithDisposer { rate ->
            targetPlayer.audioPlaybackRateProperty.set(rate?.toDouble() ?: 1.0)
        }
    }

    private fun initializeSourceContentListeners() {
        sourceTextChunks.onChangeAndDoNowWithDisposer {
            val textNodes = it.mapIndexed { index, chunkText ->
                buildChunkText(chunkText, index)
            }.toMutableList()

            textNodes.add(buildLicenseText()) // append license at bottom of the list
            sourceTextChunksContainer.items.setAll(textNodes)
        }.let(listeners::add)

        sourceTextPopup.focusedProperty().onChangeWithDisposer {
            if (it == true) {
                val content = sourceTextPopup.content.firstOrNull()
                val scrollPane = content?.findChild<ScrollPane>()
                content?.focusedProperty()?.onChangeWithDisposer {
                    if (it == true) {
                        scrollPane?.requestFocus()
                    }
                }?.let(listeners::add)
                content?.requestFocus()
            }
        }.let(listeners::add)

        isMinimizedProperty.onChangeWithDisposer { isMinimized ->
            minimizeBtn.graphicProperty().value =
                if (isMinimized == true) {
                    maximizedIcon
                } else {
                    minimizedIcon
                }
        }.let(listeners::add)

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextChunksContainer.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }.let(listeners::add)

        licenseProperty.onChangeAndDoNowWithDisposer {
            licenseTextProperty.set(
                MessageFormat.format(FX.messages["licenseStatement"], it)
            )
        }.let(listeners::add)

        sourceTextProperty.onChangeAndDoNowWithDisposer {
            val chunks = it?.split("\n") ?: listOf()
            sourceTextChunks.setAll(chunks)
        }.let(listeners::add)
    }

    fun removeListeners() {
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    private fun buildTextPopupContent(): Node {
        return VBox().apply {
            addClass("source-content__text-popup__container")

            scrollpane {
                val sp = this
                addClass("source-content__text-popup__scroll")
                vgrow = Priority.ALWAYS
                maxHeightProperty().bind(
                    FX.primaryStage.scene.heightProperty().multiply(2.0/3)
                )

                vbox {
                    vgrow = Priority.ALWAYS
                    // scroll bar offsets to avoid text overrun
                    maxWidthProperty().bind(sp.widthProperty().minus(20))
                    minHeightProperty().bind(sp.heightProperty().minus(10))

                    label {
                        addClass("source-content__text", "source-content__text-popup__title")
                        textProperty().bind(contentTitleProperty)
                    }
                    label {
                        addClass("source-content__text", "source-content__text-popup__text")
                        textProperty().bind(sourceTextProperty)
                    }
                    region { vgrow = Priority.ALWAYS }
                    label {
                        addClass("source-content__text-popup__license-text")
                        textProperty().bind(licenseTextProperty)
                    }
                }
            }
        }
    }

    private fun toggleBody() {
        isMinimizedProperty.set(!isMinimizedProperty.value)
    }

    private fun textZoom(delta: Int) {
        val zoomTo = zoomRateProperty.value + delta
        if (zoomTo < 50 || zoomTo > 200) {
            return
        }
        zoomRateProperty.set(zoomTo)
        /* notify listeners to save zoom preference */
        FX.eventbus.fire(SourceTextZoomRateChangedEvent(zoomTo))
    }

    private fun setPopupTheme(popUp: Popup) {
        FX.primaryStage.scene.root.styleClass.let {
            if (it.contains(ColorTheme.DARK.styleClass)) {
                popUp.scene.root.addClass(ColorTheme.DARK.styleClass)
                popUp.scene.root.removeClass(ColorTheme.LIGHT.styleClass)
            } else {
                popUp.scene.root.addClass(ColorTheme.LIGHT.styleClass)
                popUp.scene.root.removeClass(ColorTheme.DARK.styleClass)
            }
        }
    }

    private fun buildChunkText(textContent: String, index: Int): Label {
        return Label(textContent).apply {
            addClass("source-content__text")
            minHeight = Region.USE_PREF_SIZE // avoid ellipsis

            maxWidthProperty().bind(
                sourceTextChunksContainer.widthProperty().minus(60) // scrollbar offset
            )

            highlightedChunk.onChangeAndDoNowWithDisposer { highlightedIndex ->
                val isHighlighted = highlightedIndex == index
                toggleClass("source-content__text--highlighted", isHighlighted)
                if (isHighlighted) {
                    sourceTextChunksContainer.scrollTo(index)
                }
            }.let(listeners::add)
        }
    }

    private fun buildLicenseText(): Label {
        return Label().apply {
            addClass("source-content__license-text")

            prefWidthProperty().bind(
                sourceTextChunksContainer.widthProperty().minus(60)
            )
            textProperty().bind(licenseTextProperty)
            styleProperty().bind(orientationProperty.objectBinding {
                when (it) {
                    NodeOrientation.LEFT_TO_RIGHT -> "-fx-font-style: italic;"
                    else -> ""
                }
            })
        }
    }

    /**
     * Allow focusing when list is scrollable (overflow the bound height).
     * Unlike ScrollPane, the scrollbar of ListView is dynamically managed.
     * This binding should be established after the construction of the ListView.
     */
    private fun bindListFocusableWithScrollable() {
        sourceTextChunksContainer.apply {
            focusTraversableProperty().bind(
                Bindings.createBooleanBinding(
                {
                    getScrollBar(Orientation.VERTICAL)?.isVisible ?: false
                },
                skinProperty().select {
                    getScrollBar(Orientation.VERTICAL)?.visibleProperty()
                        ?: visibleProperty()
                }
            ))
        }
    }

    private fun ListView<*>.getScrollBar(orientation: Orientation): ScrollBar? {
        return lookupAll(".scroll-bar").firstOrNull {
            it is ScrollBar && it.orientation == orientation
        } as? ScrollBar
    }
}
