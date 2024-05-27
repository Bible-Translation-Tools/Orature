/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import com.github.thomasnield.rxkotlinfx.toObservable
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.animation.AnimationTimer
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.createAudioScrollBar
import org.wycliffeassociates.otter.jvm.controls.event.TranslationNavigationEvent
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ConsumeViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.startAnimationTimer
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class Consume : View() {
    private val logger = LoggerFactory.getLogger(Consume::class.java)

    val viewModel: ConsumeViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private val audioScrollBar = createAudioScrollBar(
        viewModel.audioPositionProperty,
        viewModel.totalFramesProperty,
        viewModel.isPlayingProperty,
        viewModel::seek
    )

    private var timer: AnimationTimer? = null
    private val eventSubscriptions = mutableListOf<EventRegistration>()

    override fun onDock() {
        super.onDock()
        logger.info("Consume docked")
        timer = startAnimationTimer { viewModel.calculatePosition() }
        viewModel.subscribeOnWaveformImagesProperty.set(::subscribeOnWaveformImages)
        viewModel.cleanupWaveformProperty.set(waveform::cleanup)
        viewModel.onDockConsume()
        waveform.initializeMarkers()
        waveform.markers.bind(viewModel.markers) { it }
        subscribeEvents()
        subscribeOnThemeChange()
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Consume undocked")
        timer?.stop()
        viewModel.onUndockConsume()
        unsubscribeEvents()
    }

    private fun subscribeOnThemeChange() {
        settingsViewModel.appColorMode
            .toObservable()
            .subscribe {
                viewModel.onThemeChange()
                waveform.initializeMarkers()
                waveform.markers.bind(viewModel.markers) { it }
            }.addTo(viewModel.compositeDisposable)
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
                    addClass("waveform--focusable")
                    vgrow = Priority.ALWAYS
                    clip = Rectangle().apply {
                        widthProperty().bind(this@vbox.widthProperty())
                        heightProperty().bind(this@vbox.heightProperty())
                    }
                    themeProperty.bind(settingsViewModel.appColorMode)
                    positionProperty.bind(viewModel.positionProperty)
                    audioPositionProperty.bind(viewModel.audioPositionProperty)
                    canMoveMarkerProperty.set(false)
                    canDeleteMarkerProperty.set(false)

                    setUpWaveformActionHandlers()

                    // Marker stuff
                    this.markers.bind(viewModel.markers) { it }
                }
                add(waveform)
                audioScrollBar
                add(audioScrollBar)
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

    private fun subscribeEvents() {
        addShortcut()

        subscribe<TranslationNavigationEvent> {
            viewModel.cleanupWaveform()
        }.also { eventSubscriptions.add(it) }
    }

    private fun unsubscribeEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
        removeShortcut()
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.PLAY_SOURCE.value, viewModel::mediaToggle)
    }

    private fun removeShortcut() {
        workspace.accelerators.remove(Shortcut.PLAY_SOURCE.value)
    }
}
