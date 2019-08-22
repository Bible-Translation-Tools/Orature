package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.View
import tornadofx.vbox

class RecorderView : View() {

    val info = InfoFragment()
    val waveform = RecordingVisualizerFragment()
    val control = ControlFragment()

    val recorderViewModel: RecorderViewModel by inject()

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add(info)
        add(waveform)
        add(control)
    }

    init {
        // notifies viewmodel that views have been inflated and the canvas now has a width
        primaryStage.setOnShown {
            recorderViewModel.onViewReady()
        }
    }
}