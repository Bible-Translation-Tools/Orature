/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.utils.fitToHeight
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MainWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WaveformFrame(
    markerTrack: MarkerTrackControl,
    mainWaveform: MainWaveform,
    // timecodeHolder: TimecodeHolder,
    viewModel: VerseMarkerViewModel
) : BorderPane() {

    init {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            translateXProperty().bind(
                viewModel
                    .positionProperty
                    .negate()
                    .plus(
                        this@WaveformFrame.widthProperty().divide(2.0)
                    )
            )

            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            top {
                region {
                    styleClass.add("vm-waveform-frame__top-track")
                    add(markerTrack)
                }
            }

            center {
                region {

                    stackpane {
                        styleClass.add("vm-waveform-frame__center")
                        alignment = Pos.CENTER

                        fitToParentHeight()
                        hbox{
                            this@hbox.bindChildren(viewModel.imageList) {
                                imageview(it) {
                                    this.fitToHeight(this@region)
                                }
                            }
                        }

                        viewModel.markers.highlightState.forEach {
                            add(
                                Rectangle().apply {
                                    managedProperty().set(false)
                                    heightProperty().bind(this@stackpane.heightProperty())
                                    widthProperty().bind(it.width)
                                    translateXProperty().bind(it.translate)
                                    visibleProperty().bind(it.visibility)
                                    it.styleClass.onChangeAndDoNow {
                                        styleClass.setAll(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
    //                add(timecodeHolder)
                }
            }
        }
    }
}
