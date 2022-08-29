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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.Fragment
import tornadofx.gridpane
import tornadofx.hgrow
import tornadofx.vgrow

class RecordingVisualizerFragment : Fragment() {

    private val vm: RecorderViewModel by inject()
    private val sourceContent = find<SourceAudioFragment>()

    override val root = gridpane {

        // these are needed for the gridpane itself to fill out the entire width/height allocated to it
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        styleClass.add("waveform-container")

        // you seem to just add things to a column and row index without allocating the number of rows or columns first
        // the classname fragment currently lies - you apparently can only add nodes, not uicomponents? fragment is the latter

        var waveformGridIndex = 0
        var volumeBarGridIndex = 1

        if (vm.narrationMode) {
            waveformGridIndex = 1
            volumeBarGridIndex = 2
            this.add(sourceContent.root, 0, 0)
        }
        this.add(vm.waveformView, waveformGridIndex, 0)
        this.add(vm.volumeBarView, volumeBarGridIndex, 0)

        // these constraints provide the min/pref/max width, hgrow, hpos, and fill width
        val sourceContentConstraint = ColumnConstraints(
            0.0,
            0.0,
            Double.MAX_VALUE,
            Priority.ALWAYS,
            HPos.LEFT,
            true
        )
        val waveformColumnConstraint = ColumnConstraints(
            0.0,
            0.0,
            Double.MAX_VALUE,
            Priority.ALWAYS,
            HPos.LEFT,
            true
        )
        val volumeBarColumnConstraint = ColumnConstraints(
            25.0,
            25.0,
            25.0,
            Priority.SOMETIMES,
            HPos.RIGHT,
            true
        )

        // adding will increment an internal counter, so the first constraint is on the first column, second on second
        if (vm.narrationMode) {
            sourceContentConstraint.percentWidth = 40.0
            this.columnConstraints.add(sourceContentConstraint)
        }
        this.columnConstraints.add(waveformColumnConstraint)
        this.columnConstraints.add(volumeBarColumnConstraint)

        // this constraint is necessary for the row to fill out the height of this gridpane
        this.rowConstraints.addAll(
            RowConstraints(
                0.0,
                0.0,
                Double.MAX_VALUE,
                Priority.ALWAYS,
                VPos.CENTER,
                true
            )
        )
    }
}
