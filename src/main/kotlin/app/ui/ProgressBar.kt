package app.ui

import app.ui.ChartCSS.Companion.progressBarStyle
import javafx.application.Platform
import javafx.geometry.Pos
import tornadofx.*
import java.util.*
import kotlin.concurrent.thread

/**
 * sweet code for a heckin cool loading bar
 */
class ProgressBar : View() {
    init {// pulls correct language
        messages = ResourceBundle.getBundle("MyView")}
    override val root = vbox {
        var isItDone = false

        style {
            alignment = Pos.TOP_CENTER
        }

        progressbar {
            addClass(progressBarStyle)
            thread {
                // change this to a "while profile is not generated"
                // how will we know when it IS generated?
                while (!isItDone) {
                    for (i in 1..100) {
                        Platform.runLater { progress = i.toDouble() / 100.0 }
                        Thread.sleep(10)
                    }
                }
            }
        }
        label(messages["generatingProfileText"])
    }
}