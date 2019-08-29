package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.application.Platform
import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.Fragment
import tornadofx.vbox

class RecorderView : Fragment() {

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
        if (primaryStage.isShowing) {
            recorderViewModel.onViewReady()
        } else {
            primaryStage.setOnShown {
                recorderViewModel.onViewReady()
            }
        }
        Thread {
            Thread.sleep(1000)
            Platform.runLater {
                recorderViewModel.onViewReady()

            }
        }.start()

    }
}