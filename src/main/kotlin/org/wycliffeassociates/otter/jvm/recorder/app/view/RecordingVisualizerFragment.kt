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

    override val root = gridpane {

        //these are needed for the gridpane itself to fill out the entire width/height allocated to it
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        //you seem to just add things to a column and row index without allocating the number of rows or columns first
        //the classname fragment currently lies- you apparently can only add nodes, not uicomponents? fragment is the latter
        this.add(vm.waveformView, 0, 0)
        this.add(vm.volumeBarView, 1, 0)

        //these constraints provide the min/pref/max width, hgrow, hpos, and fill width
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

        //adding will increment an internal counter, so the first constraint is on the first column, second on second
        this.columnConstraints.add(waveformColumnConstraint)
        this.columnConstraints.add(volumeBarColumnConstraint)

        //this constraint is necessary for the row to fill out the height of this gridpane
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