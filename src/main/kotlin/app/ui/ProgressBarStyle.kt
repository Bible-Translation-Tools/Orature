package app.ui

import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*



class ProgressBarStyle: Stylesheet() {
    companion object {
        val progressBarStyle by cssclass()
    }

    init {

        progressBarStyle {
            alignment = Pos.TOP_CENTER
            minWidth = 250.px
            minHeight = 50.px




        }

    }
}

