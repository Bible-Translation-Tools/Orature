package app.ui.widgets.recordButton

import app.ui.ProgressBar
import app.ui.styles.ButtonStyles
import app.ui.userCreation.UserCreation

import app.ui.userCreation.ViewModel.UserCreationViewModel
import app.ui.widgets.ProfilePreview

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import app.ui.widgets.RecordingAnimation
import app.ui.widgets.profileIcon.ProfileIcon
import java.util.*
import kotlin.concurrent.timerTask

class RecordButton : VBox() {


    val circle = RecordingAnimation()
    val dotsAn = DotsAnimation()
    val UserCreationViewModel = UserCreationViewModel()
    val countdown = UserCreationViewModel.countdownTracker


    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")

    val recButton= button(countdown, micIcon){
        importStylesheet(ButtonStyles::class)
        addClass(ButtonStyles.roundButton)
        style {
            backgroundColor += Color.WHITE
            micIcon.fill = c("#CC4141")
            cursor = Cursor.HAND
            minWidth = 152.0.px
            minHeight = 152.0.px
            fontSize = 8.em
            textFill = c("#CC4141")

        }

        action {
            dotsAn.showCircles()
            micIcon.hide()
            var timer = Timer()
            UserCreationViewModel.countdown()

            timer.schedule(timerTask { Platform.runLater {
                dotsAn.hide()
                circle.animate()
                graphic = stopIcon
                stopIcon.fill=c("#CC4141")

            } }, 3000)

            timer.schedule(timerTask {
                Platform.runLater {
                    circle.hide()

                    val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash


                    replaceWith(ProfilePreview(randomNumber.toString()), transition = ViewTransition.Fade(.2.seconds))

                }
            }, 6100)


        }
    }

     val wrapper =
         stackpane {
             alignment = Pos.CENTER

             add(circle)
             add(recButton)


         }



   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       style{
           padding= box(20.px)
       }
       add(dotsAn)
   }


}
