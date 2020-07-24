package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.stage.Screen
import tornadofx.*

const val WINDOW_OFFSET = 50.0

class MarkerView : View() {

    private val userAgentStylesheet = javaClass.getResource("/css/verse-marker-app.css").toExternalForm()

    init {
        FX.stylesheets.setAll(userAgentStylesheet)
    }

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val playbackControls = PlaybackControlsFragment()

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - WINDOW_OFFSET
        prefWidth = Screen.getPrimary().visualBounds.width - WINDOW_OFFSET

        add(titleFragment)
        add(minimap)
        add(waveformContainer)
        add(playbackControls)
    }
}
