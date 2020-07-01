package org.wycliffeassociates.otter.jvm.markerapp.app

import javafx.stage.Screen
import tornadofx.*

class MarkerView: View() {
    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add(TitleFragment())
    }
}