package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

const val WINDOW_OFFSET = 50.0

class MarkerView : PluginEntrypoint() {

    val viewModel: VerseMarkerViewModel by inject()

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val source = SourceAudioFragment()
    val playbackControls = PlaybackControlsFragment()

    init {
        runLater {
            val css = this@MarkerView.javaClass.getResource("/css/verse-marker-app.css")
                .toExternalForm()
                .replace(" ", "%20")
            importStylesheet(css)

            FX.stylesheets.addAll(
                javaClass.getResource("/css/control.css").toExternalForm(),
                css
            )
        }
        viewModel.initializeAudioController(minimap.slider)
    }

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - WINDOW_OFFSET
        prefWidth = Screen.getPrimary().visualBounds.width - WINDOW_OFFSET

        add(titleFragment)
        add(minimap)
        add(waveformContainer)
        add(source)
        add(playbackControls)
    }
}
