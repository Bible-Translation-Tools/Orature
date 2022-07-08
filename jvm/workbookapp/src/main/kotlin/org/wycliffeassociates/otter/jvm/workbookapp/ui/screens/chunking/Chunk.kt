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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXButton
import com.sun.javafx.util.Utils
import java.text.MessageFormat
import javafx.animation.AnimationTimer
import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingWizardPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.get
import tornadofx.hbox

class Chunk : Fragment() {

    val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val nextIcon = FontIcon(Material.SKIP_NEXT)
    private val previousIcon = FontIcon(Material.SKIP_PREVIOUS)

    private val rootStyles = "chunking-play-controls"
    private val playButtonStyle = "chunking-play-controls__play-btn"
    private val roundedButtonStyle = "chunking-play-controls__btn--rounded"
    private val seekButtonStyle = "chunking-play-controls__seek-btn"
    private val continueButtonStyle = "chunking-continue-button"

    val vm: ChunkingViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private val markerTrack: MarkerTrackControl = MarkerTrackControl()

    var timer: AnimationTimer? = null

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/scrolling-waveform.css"))
        tryImportStylesheet(resources.get("/css/chunk-marker.css"))
        tryImportStylesheet(resources.get("/css/chunk-page.css"))
        vm.onDockConsume()
        vm.pageProperty.set(ChunkingWizardPage.CHUNK)
        vm.titleProperty.set(messages["chunkingTitle"])
        vm.stepProperty.set(messages["chunkingDescription"])

        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                vm.calculatePosition()
            }
        }
        timer?.start()
        vm.onDockChunk()

        markerTrack.apply {
            prefWidth = vm.imageWidth
            vm.markerStateProperty.onChangeAndDoNow { markers ->
                markers?.let { markers ->
                    markers.markerCountProperty?.onChangeAndDoNow {
                        highlightState.setAll(vm.markers.highlightState)
                        this.markers.setAll(vm.markers.markers)
                        refreshMarkers()
                    }
                }
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        timer?.stop()
        vm.compositeDisposable.clear()
    }

    override val root = borderpane {
        center = MarkerPlacementWaveform(markerTrack).apply {
            themeProperty.bind(settingsViewModel.appColorMode)
            positionProperty.bind(vm.positionProperty)

            vm.compositeDisposable.add(
                vm.waveform.observeOnFx().subscribe { addWaveformImage(it) }
            )

            onWaveformClicked = { vm.pause() }
            onWaveformDragReleased = { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = vm.getLocationInFrames()
                val duration = vm.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                vm.seek(final)
            }

            onPlaceMarker = vm::placeMarker

            markerStateProperty.bind(vm.markerStateProperty)
        }
        bottom = hbox {
            styleClass.addAll("consume__bottom")
            styleClass.add(rootStyles)
            alignment = Pos.CENTER
            val nextBtn = JFXButton().apply {
                graphic = nextIcon
                setOnAction { vm.seekNext() }
                styleClass.addAll(
                    seekButtonStyle,
                    roundedButtonStyle
                )
            }

            val previousBtn = JFXButton().apply {
                graphic = previousIcon
                setOnAction { vm.seekPrevious() }
                styleClass.addAll(
                    seekButtonStyle,
                    roundedButtonStyle
                )
            }
            add(previousBtn)

            button {
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
            add(nextBtn)
        }
    }
}

