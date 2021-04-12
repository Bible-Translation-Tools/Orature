package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.stage.Screen
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

class RecorderView : PluginEntrypoint() {

    private var viewInflated = false

    private val info = InfoFragment()
    private val waveform = RecordingVisualizerFragment()
    private val control = ControlFragment()
    private val source = SourceAudioFragment()

    override val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["recording"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_MICROPHONE))
    }

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
        runLater {
            val css = this@RecorderView.javaClass.getResource("/css/recorder.css")
                .toExternalForm()
                .replace(" ", "%20")
            importStylesheet(css)
        }

        // notifies viewmodel that views have been inflated and the canvas now has a width
        waveform.root.widthProperty().onChange { width ->
            if (!viewInflated && width.toInt() > 0) {
                recorderViewModel.onViewReady(width.toInt())
                viewInflated = true
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        recorderViewModel.save()
    }
}
