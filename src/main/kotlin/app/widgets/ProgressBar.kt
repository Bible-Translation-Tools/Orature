package app.ui


import app.UIColorsObject.Colors
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.layout.VBox


import tornadofx.*
import tornadofx.FX.Companion.messages
import kotlin.concurrent.thread

/**
 * sweet code for a heckin cool loading bar
 */
class ProgressBar : VBox() {
    init {
        importStylesheet("/progressbar.css")
        var isItDone = false

        style {
            alignment = Pos.CENTER
            backgroundColor += c("#FFF")
        }
        label(messages["generatingProfileText"]) { addClass("headerText") }
        progressbar {
            addClass("progress-bar")
            //addClass(progressBarStyle)
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
    }

}