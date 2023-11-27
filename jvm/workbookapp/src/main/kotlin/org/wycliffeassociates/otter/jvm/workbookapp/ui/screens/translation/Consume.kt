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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.animation.AnimationTimer
import javafx.scene.control.Slider
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ConsumeViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.startAnimationTimer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class Consume : View() {
    private val logger = LoggerFactory.getLogger(Consume::class.java)

    val viewModel: ConsumeViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private lateinit var scrollbarSlider: Slider

    private var cleanUpWaveform: () -> Unit = {}
    private var timer: AnimationTimer? = null

    override fun onDock() {
        super.onDock()
        logger.info("Consume docked")
        timer = startAnimationTimer { viewModel.calculatePosition() }
        viewModel.audioController = AudioPlayerController(scrollbarSlider)
        viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
        viewModel.onDockConsume()
        waveform.markers.bind(viewModel.markers) { it }
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Consume undocked")
        timer?.stop()
        cleanUpWaveform()
        viewModel.onUndockConsume()
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }

    override val root = vbox {
        borderpane {
            vgrow = Priority.ALWAYS

            center = VBox().apply {
                MarkerWaveform().apply {
                    waveform = this
                    addClass("consume__scrolling-waveform")
                    vgrow = Priority.ALWAYS
                    clip = Rectangle().apply {
                        widthProperty().bind(this@vbox.widthProperty())
                        heightProperty().bind(this@vbox.heightProperty())
                    }
                    themeProperty.bind(settingsViewModel.appColorMode)
                    positionProperty.bind(viewModel.positionProperty)
                    canMoveMarkerProperty.set(false)
                    canDeleteMarkerProperty.set(false)

                    setUpWaveformActionHandlers()
                    cleanUpWaveform = ::freeImages

                    // Marker stuff
                    this.markers.bind(viewModel.markers) { it }
                }
                scrollbarSlider = createAudioScrollbarSlider()
                add(waveform)
                add(scrollbarSlider)
            }
            bottom = hbox {
                addClass("consume__bottom")
                button {
                    addClass("btn", "btn--primary", "consume__btn")
                    val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
                    val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
                    textProperty().bind(viewModel.isPlayingProperty.stringBinding {
                        togglePseudoClass("active", it == true)
                        if (it == true) {
                            graphic = pauseIcon
                            messages["pause"]
                        } else {
                            graphic = playIcon
                            messages["playSource"]
                        }
                    })

                    action {
                        viewModel.mediaToggle()
                    }
                }
            }
        }
    }

    private fun setUpWaveformActionHandlers() {
        waveform.apply {
            setOnSeekNext { viewModel.seekNext() }
            setOnSeekPrevious { viewModel.seekPrevious() }
            setOnWaveformClicked { viewModel.pause() }
            setOnWaveformDragReleased { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = viewModel.getLocationInFrames()
                val duration = viewModel.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                viewModel.seek(final)
            }
            setOnRewind(viewModel::rewind)
            setOnFastForward(viewModel::fastForward)
            setOnToggleMedia(viewModel::mediaToggle)
            setOnResumeMedia(viewModel::resumeMedia)
        }
    }

    private fun createAudioScrollbarSlider(): Slider {
        return AudioSlider().apply {
            hgrow = Priority.ALWAYS
            colorThemeProperty.bind(settingsViewModel.selectedThemeProperty)
            setPixelsInHighlightFunction { viewModel.pixelsInHighlight(it) }
            player.bind(viewModel.waveformAudioPlayerProperty)
            secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
        }
    }
}
