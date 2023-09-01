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
import com.sun.glass.ui.Screen
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.scene.control.Slider
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import kotlin.math.max

class Consume : Fragment() {
    private val logger = LoggerFactory.getLogger(Consume::class.java)

    val viewModel: ChunkingViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerPlacementWaveform
    private lateinit var slider: Slider

    /** put these in VM */
    private var sampleRate: Int = 0 // beware of divided by 0
    private var totalFrames: Int = 0 // beware of divided by 0

    override fun onDock() {
        super.onDock()
        logger.info("Consume docked")

        val wb = viewModel.workbookDataStore.workbook
        val chapter = wb.target.chapters.blockingFirst()
        val sourceAudio = wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
        viewModel.audioDataStore.sourceAudioProperty.set(sourceAudio)
        viewModel.workbookDataStore.activeChapterProperty.set(chapter)

        viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
        viewModel.onDockConsume()
        viewModel.initializeAudioController(slider)
        viewModel.audioPlayer.value.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        waveform.markers.bind(viewModel.markers) { it }
    }

    override fun onUndock() {
        super.onUndock()
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
                MarkerPlacementWaveform().apply {
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

                    setOnSeekNext { viewModel.seekNext() }
                    setOnSeekPrevious { viewModel.seekPrevious() }
                    setOnPlaceMarker { viewModel.placeMarker() }
                    setOnWaveformClicked { viewModel.pause() }
                    setOnWaveformDragReleased { deltaPos ->
                        val deltaFrames = org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames(deltaPos)
                        val curFrames = viewModel.getLocationInFrames()
                        val duration = viewModel.getDurationInFrames()
                        val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                        viewModel.seek(final)
                    }
                    setOnRewind(viewModel::rewind)
                    setOnFastForward(viewModel::fastForward)
                    setOnToggleMedia(viewModel::mediaToggle)
                    setOnResumeMedia(viewModel::resumeMedia)

                    // Marker stuff
                    imageWidthProperty.bind(viewModel.imageWidthProperty)

                    this.markers.bind(viewModel.markers) { it }
                }
                slider = AudioSlider().apply {
                    hgrow = Priority.ALWAYS
                    colorThemeProperty.bind(settingsViewModel.selectedThemeProperty)
                    setPixelsInHighlightFunction { pixelsInHighlight(it) }
                    player.bind(viewModel.audioPlayer)
                    secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
                }
                add(waveform)
                add(slider)
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

    /** go to VM */
    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || totalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = totalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }
}

fun pixelsToFrames(pixels: Double): Int {
    val framesOnScreen = SECONDS_ON_SCREEN * 44100
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth
    return (pixels * framesInPixel).toInt()
}
