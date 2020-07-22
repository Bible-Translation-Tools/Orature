package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class PlaybackControlsFragment : Fragment() {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")
    private val NEXT_ICON = FontIcon("gmi-skip-next")
    private val PREVIOUS_ICON = FontIcon("gmi-skip-previous")

    private val playBtn = button {
        styleClass.addAll(
            "vm-play-controls__play-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = PLAY_ICON
    }

    private val nextBtn = button {
        styleClass.addAll(
            "vm-play-controls__seek-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = NEXT_ICON
    }

    private val previousBtn = button {
        styleClass.addAll(
            "vm-play-controls__seek-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = PREVIOUS_ICON
    }

    override val root = hbox {
        alignment = Pos.CENTER
        styleClass.add("vm-play-controls")
        add(previousBtn)
        add(playBtn)
        add(nextBtn)
    }
}
