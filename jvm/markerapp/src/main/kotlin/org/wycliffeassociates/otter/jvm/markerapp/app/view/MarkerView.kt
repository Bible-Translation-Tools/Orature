package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.stage.Screen
import tornadofx.*

class MarkerView : View() {

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/verse-marker-app.css").toExternalForm()

    init {
        FX.stylesheets.setAll(USER_AGENT_STYLESHEET)
    }

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val playbackControls = PlaybackControlsFragment()

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add(titleFragment)
        add(minimap)
        add(waveformContainer)
        add(playbackControls)
    }
}
