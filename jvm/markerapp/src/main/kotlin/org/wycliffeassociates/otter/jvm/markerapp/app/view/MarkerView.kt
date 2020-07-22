package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class MarkerView : View() {

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/verse-marker-app.css").toExternalForm()

    val vm: VerseMarkerViewModel by inject()

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val playbackControls = PlaybackControlsFragment()

    init {
        FX.stylesheets.setAll(USER_AGENT_STYLESHEET)
        vm.initializeAudioController(minimap.slider)
    }

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add(titleFragment)
        add(minimap)
        add(waveformContainer)
        add(playbackControls)
    }
}
