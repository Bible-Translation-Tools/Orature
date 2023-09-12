/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import kfoenix.jfxbutton
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel

// TODO: DELETE THIS CLASS
class ChunkingPage : View() {

    val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val nextIcon = FontIcon(Material.SKIP_NEXT)
    private val previousIcon = FontIcon(Material.SKIP_PREVIOUS)

    private val rootStyles = "chunking-play-controls"
    private val playButtonStyle = "chunking-play-controls__play-btn"
    private val roundedButtonStyle = "chunking-play-controls__btn--rounded"
    private val seekButtonStyle = "chunking-play-controls__seek-btn"

    val vm: ChunkingViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    val waveform = MarkerPlacementWaveform().apply {
        addClass("chunking__marker-waveform")

        themeProperty.bind(settingsViewModel.appColorMode)
        positionProperty.bind(vm.positionProperty)

        vm.chunkImageCleanup = ::freeImages

        setOnWaveformClicked { vm.pause() }
        setOnWaveformDragReleased { deltaPos ->
            val deltaFrames = pixelsToFrames(deltaPos)
            val curFrames = vm.getLocationInFrames()
            val duration = vm.getDurationInFrames()
            val final = Utils.clamp(0, curFrames - deltaFrames, duration)
            vm.seek(final)
        }

        setOnToggleMedia(vm::mediaToggle)
        setOnRewind(vm::rewind)
        setOnFastForward(vm::fastForward)

        setOnPlaceMarker(vm::placeMarker)

        imageWidthProperty.bind(vm.imageWidthProperty)
    }

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/scrolling-waveform.css"))
        tryImportStylesheet(resources.get("/css/chunk-marker.css"))
        tryImportStylesheet(resources.get("/css/chunk-page.css"))

        vm.subscribeOnWaveformImages = ::subscribeOnWaveformImages

//        vm.onDockChunk()

        waveform.markers.bind(vm.markers, { it })
        waveform.refreshMarkers()
    }

    override fun onUndock() {
        super.onUndock()
        vm.onUndockChunk()
    }

    private lateinit var leftControls: Region

    override val root = borderpane {
        center = waveform
        bottom = hbox {
            styleClass.addAll("consume__bottom")
            styleClass.add(rootStyles)
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER

            borderpane {
                left {
                    hbox {
                        leftControls = this
                    }
                }
                center {
                    hgrow = Priority.ALWAYS

                    hbox {
                        addClass("chunking__controls-group")
                        alignment = Pos.CENTER

                        jfxbutton {
                            graphic = previousIcon
                            setOnAction { vm.seekPrevious() }
                            styleClass.addAll(
                                seekButtonStyle,
                                roundedButtonStyle
                            )
                        }

                        jfxbutton {
                            vm.isPlayingProperty.onChangeAndDoNow {
                                it?.let {
                                    when (it) {
                                        true -> graphic = pauseIcon
                                        false -> graphic = playIcon
                                    }
                                }
                            }
                            styleClass.addAll(
                                playButtonStyle,
                                roundedButtonStyle
                            )
                            action {
                                vm.mediaToggle()
                            }
                        }
                        jfxbutton {
                            graphic = nextIcon
                            setOnAction { vm.seekNext() }
                            styleClass.addAll(
                                seekButtonStyle,
                                roundedButtonStyle
                            )
                        }
                    }
                }

                right {
                    hbox {
                        addClass("chunking__controls-group")
                        alignment = Pos.CENTER_RIGHT
                        leftControls.prefWidthProperty().bind(this.widthProperty())

                        jfxbutton(messages["undo"]) {
                            addClass("btn", "btn--secondary", "btn--white-on-dark")
                            setOnAction {
                                vm.undoMarker()
                            }
                        }
                        jfxbutton(messages["redo"]) {
                            addClass("btn", "btn--secondary", "btn--white-on-dark")
                            setOnAction {
                                vm.redoMarker()
                            }
                        }
                        jfxbutton(messages["save"]) {
                            addClass("btn", "btn--primary", "btn--borderless")
                            setOnAction {
                                vm.saveChanges()
                                workspace.navigateBack()
                            }
                        }
                    }
                }
            }
        }

        shortcut(Shortcut.ADD_MARKER.value, vm::placeMarker)
    }

    private fun subscribeOnWaveformImages() {
        vm.compositeDisposable.add(
            vm.waveform.observeOnFx().subscribe {
                (root.center as MarkerPlacementWaveform).addWaveformImage(it)
            }
        )
    }
}
