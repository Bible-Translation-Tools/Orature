package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.*

class RecorderView : PluginEntrypoint() {

    private var viewInflated = false

    private val info = InfoFragment()
    private val waveform = RecordingVisualizerFragment()
    private val control = ControlFragment()
    private val source = SourceAudioFragment()

    private val spacer = region().apply {
        prefHeight = 2.0
    }

    private val recorderViewModel: RecorderViewModel by inject()

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add(info)
        add(spacer)
        add(waveform)
        add(source)
        add(control)
    }

    init {
        root.stylesheets.add(javaClass.getResource("/css/recorder.css").toExternalForm())

        // notifies viewmodel that views have been inflated and the canvas now has a width
        waveform.root.widthProperty().onChange { width ->
            if (!viewInflated && width.toInt() > 0) {
                recorderViewModel.onViewReady(width.toInt())
                viewInflated = true
            }
        }
    }
}