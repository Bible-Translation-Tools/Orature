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
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import javafx.geometry.Orientation
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.event.MarkerMovedEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestBlockedEvent
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseRequestEvent
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

class MarkerView : PluginEntrypoint() {
    val viewModel: VerseMarkerViewModel by inject()

    private val waveform = MarkerPlacementWaveform()

    private var slider: AudioSlider? = null
    private var minimap: MinimapFragment? = null

    private val disposables = mutableListOf<ListenerDisposer>()

    override fun onDock() {
        super.onDock()
        viewModel.onDock {
            viewModel.compositeDisposable.add(
                viewModel.waveform.observeOnFx().subscribe { waveform.addWaveformImage(it) }
            )
        }
        slider?.let {
            viewModel.initializeAudioController(it)
        }
        waveform.markers.bind(viewModel.markers) { it }
        waveform.initializeMarkers()
        viewModel.cleanupWaveform = waveform::cleanup
    }

    init {
        tryImportStylesheet(resources["/css/verse-marker-app.css"])
        tryImportStylesheet(resources["/css/scrolling-waveform.css"])
        tryImportStylesheet(resources["/css/chunk-marker.css"])

        subscribe<PluginCloseRequestEvent> {
            unsubscribe()
            viewModel.saveAndQuit()
        }

        subscribe<NavigationRequestBlockedEvent> {
            unsubscribe()
            viewModel.saveAndQuit()
        }

        subscribe<MarkerMovedEvent> {
            viewModel.moveMarker(it.markerId, it.start, it.end)
        }
    }

    override val root = splitpane(Orientation.HORIZONTAL) {
        setDividerPositions(0.33)
        addClass("vm-split-container")

        vbox {
            add(
                SourceTextFragment().apply {
                    highlightedChunkNumberProperty.bind(viewModel.currentMarkerNumberProperty)
                }
            )
        }

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
                audioPositionProperty.bind(viewModel.audioPositionProperty)

                setOnSeekNext { viewModel.seekNext() }
                setOnSeekPrevious { viewModel.seekPrevious() }
                setOnPlaceMarker { viewModel.placeMarker() }
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

                // Marker stuff
                imageWidthProperty.bind(viewModel.imageWidthProperty)

                setOnPositionChanged { id, position -> slider!!.updateMarker(id, position) }
                setOnLocationRequest { viewModel.requestAudioLocation() }
            }
            bottom = vbox {
                add<PlaybackControlsFragment>()
            }
            shortcut(Shortcut.ADD_MARKER.value, viewModel::placeMarker)
            shortcut(Shortcut.GO_BACK.value, viewModel::saveAndQuit)
        }
    }
}
