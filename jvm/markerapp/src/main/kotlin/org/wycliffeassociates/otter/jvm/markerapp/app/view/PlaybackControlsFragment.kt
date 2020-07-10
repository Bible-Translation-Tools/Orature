package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class PlaybackControlsFragment : Fragment() {
    private val vm: VerseMarkerViewModel by inject()

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    private val playBtn = button { graphic = PLAY_ICON }

    init {
        vm.isPlayingProperty.onChange { playing ->
            if (playing) {
                playBtn.graphicProperty().set(PAUSE_ICON)
            } else {
                playBtn.graphicProperty().set(PLAY_ICON)
            }
        }
    }

    override val root = hbox {
        alignment = Pos.CENTER

        button("Seek Left") { setOnAction { vm.seekPrevious() } }
        add(playBtn.apply { setOnAction { vm.mediaToggle() } })
        button("Seek Right") { setOnAction { vm.seekNext() } }
    }
}