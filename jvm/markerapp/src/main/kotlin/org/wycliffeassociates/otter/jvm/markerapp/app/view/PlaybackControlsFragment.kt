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
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.jfoenix.controls.JFXButton
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseRequestEvent
import tornadofx.*

class PlaybackControlsFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(PlaybackControlsFragment::class.java)

    var refreshViewProperty = {}

    val viewModel: VerseMarkerViewModel by inject()

    private val rootStyles = "vm-play-controls"
    private val playButtonStyle = "vm-play-controls__play-btn"
    private val roundedButtonStyle = "vm-play-controls__btn--rounded"
    private val seekButtonStyle = "vm-play-controls__seek-btn"
    private val continueButtonStyle = "vm-continue-button"

    private val playIcon = FontIcon("fa-play")
    private val pauseIcon = FontIcon("fa-pause")
    private val nextIcon = FontIcon("gmi-skip-next")
    private val previousIcon = FontIcon("gmi-skip-previous")
    private val continueIcon = FontIcon("fas-check")

    private lateinit var leftControls: Region

    private val playBtn =
        JFXButton().apply {
            styleClass.addAll(
                playButtonStyle,
                roundedButtonStyle,
            )
            graphicProperty().bind(
                Bindings.createObjectBinding(
                    {
                        when (viewModel.isPlayingProperty.value) {
                            true -> pauseIcon
                            false -> playIcon
                        }
                    },
                    viewModel.isPlayingProperty,
                ),
            )
            setOnAction { viewModel.mediaToggle() }
        }

    private val nextBtn =
        JFXButton().apply {
            styleClass.addAll(
                seekButtonStyle,
                roundedButtonStyle,
            )
            graphic = nextIcon
            setOnAction { viewModel.seekNext() }
        }

    private val previousBtn =
        JFXButton().apply {
            styleClass.addAll(
                seekButtonStyle,
                roundedButtonStyle,
            )
            graphic = previousIcon
            setOnAction { viewModel.seekPrevious() }
        }

    private val undoBtn =
        JFXButton().apply {
            text = messages["undo"]
            addClass("btn", "btn--secondary", "btn--white-on-dark")

            setOnAction {
                viewModel.undoMarker()
                refreshViewProperty.invoke()
            }
        }

    private val redoBtn =
        JFXButton().apply {
            text = messages["redo"]
            addClass("btn", "btn--secondary", "btn--white-on-dark")

            setOnAction {
                viewModel.redoMarker()
                refreshViewProperty.invoke()
            }
        }

    private val closeBtn =
        JFXButton().apply {
            text = messages["continue"]
            graphic = continueIcon
            styleClass.addAll("btn", "btn--primary", "btn--borderless", continueButtonStyle)

            disableProperty().bind(viewModel.isLoadingProperty)
            setOnAction {
                fire(PluginCloseRequestEvent)
            }
        }

    override val root =
        borderpane {
            styleClass.add(rootStyles)
            left =
                region {
                    leftControls = this
                }
            center =
                hbox {
                    styleClass.add(rootStyles)
                    alignment = Pos.CENTER
                    add(previousBtn)
                    add(playBtn)
                    add(nextBtn)
                }
            right =
                hbox {
                    leftControls.prefWidthProperty().bind(this.widthProperty())
                    alignment = Pos.CENTER_RIGHT
                    spacing = 10.0
                    add(undoBtn)
                    add(redoBtn)
                    add(closeBtn)
                }
        }
}
