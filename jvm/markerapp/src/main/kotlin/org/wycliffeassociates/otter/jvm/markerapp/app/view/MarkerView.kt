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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import javafx.animation.AnimationTimer
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseRequestEvent
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

class MarkerView : PluginEntrypoint() {

    var timer: AnimationTimer? = null
    val viewModel: VerseMarkerViewModel by inject()

    private val waveform = MarkerPlacementWaveform()

    private var slider: AudioSlider? = null
    private var minimap: MinimapFragment? = null

    override fun onDock() {
        super.onDock()
        viewModel.onDock {
            viewModel.compositeDisposable.add(
                viewModel.waveform.observeOnFx().subscribe { waveform.addWaveformImage(it) }
            )
        }
        viewModel.imageCleanup = waveform::freeImages
        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                viewModel.calculatePosition()
            }
        }
        timer?.start()
        slider?.let {
            viewModel.initializeAudioController(it)
        }

        waveform.markers.bind(viewModel.markers, { it })
    }

    init {
        tryImportStylesheet(resources.get("/css/verse-marker-app.css"))
        tryImportStylesheet(resources.get("/css/scrolling-waveform.css"))
        tryImportStylesheet(resources.get("/css/chunk-marker.css"))

        initThemeProperty()
        subscribe<PluginCloseRequestEvent> {
            viewModel.saveAndQuit()
        }
    }

    override fun onUndock() {
        super.onUndock()
        timer?.stop()
        timer = null
        waveform.positionProperty.unbind()
        minimap?.cleanUpOnUndock()
    }

    override val root =
        borderpane {
            top = vbox {
                add<TitleFragment>()
                add<MinimapFragment> {
                    this@MarkerView.minimap = this
                    this@MarkerView.slider = slider
                }
            }
            center = waveform.apply {
                addClass("vm-marker-waveform")
                themeProperty.bind(viewModel.themeColorProperty)
                positionProperty.bind(viewModel.positionProperty)

                onSeekNext = viewModel::seekNext
                onSeekPrevious = viewModel::seekPrevious

                onPlaceMarker = {
                    viewModel.placeMarker()
                }
                onWaveformClicked = { viewModel.pause() }
                onWaveformDragReleased = { deltaPos ->
                    val deltaFrames = pixelsToFrames(deltaPos)
                    val curFrames = viewModel.getLocationInFrames()
                    val duration = viewModel.getDurationInFrames()
                    val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                    viewModel.seek(final)
                }
                onRewind = viewModel::rewind
                onFastForward = viewModel::fastForward
                onToggleMedia = viewModel::mediaToggle
                onResumeMedia = viewModel::resumeMedia

                // Marker stuff
                imageWidthProperty.bind(viewModel.imageWidthProperty)

                onPositionChangedProperty = slider!!::updateMarker
                onLocationRequestProperty = viewModel::requestAudioLocation
            }
            bottom = vbox {
                add<SourceTextFragment> {
                    highlightedChunkNumberProperty.bind(viewModel.currentMarkerNumberProperty)
                }
                add<PlaybackControlsFragment> {
                    refreshViewProperty = {
                    }
                }
            }
            shortcut(Shortcut.ADD_MARKER.value, viewModel::placeMarker)
            shortcut(Shortcut.GO_BACK.value, viewModel::saveAndQuit)
        }

    private fun initThemeProperty() {
        primaryStage.scene.root.styleClass.onChangeAndDoNow {
            if (it.contains(ColorTheme.DARK.styleClass)) {
                viewModel.themeColorProperty.set(ColorTheme.DARK)
            } else {
                viewModel.themeColorProperty.set(ColorTheme.LIGHT)
            }
        }
    }
}
