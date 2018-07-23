package app.ui


import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


import tornadofx.*
import java.util.*
import kotlin.concurrent.thread

/**
 * sweet code for a heckin cool loading bar
 */
class ProgressBar : VBox() {

    init {

        importStylesheet("/progressbar.css")
        var messages = ResourceBundle.getBundle("MyView")

        var isItDone = false

        style {
            alignment = Pos.CENTER
            backgroundColor += c("#FFFFFF")
        }

        label(messages["generatingProfileText"]) { addClass("headerText") }


        progressbar {
            addClass("progress-bar")
            //addClass(progressBarStyle)
            thread {
                // change this to a "while profile is not generated"
                // how will we know when it IS generated?
               // while (!isItDone) {
                    for (i in 1..100) {
                        Platform.runLater { progress = i.toDouble() / 100.0 }
                        Thread.sleep(10)
                    }
               // }
            }
        }
    }

}