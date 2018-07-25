package app.widgets.recordButton


import app.ui.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*


import java.util.*
import kotlin.concurrent.timerTask

class RecordButton(var onClickCallback: () -> Unit = ::println, var animationCompletedCallback : () -> Unit = ::println, var stopClickedCallback: () -> Unit= ::println) : VBox() {


    val circle = RecordingAnimation()
    val dotsAn = DotsAnimation()
    val RecordButtonViewModel = RecordButtonViewModel()
    val countdown = RecordButtonViewModel.countdownTracker
    val isRecording = RecordButtonViewModel.isRecording
    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")

    var timer = Timer()
     val wrapper =
         stackpane {
             alignment = Pos.CENTER

             add(circle)
             button(countdown, micIcon) {
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
                     if (isRecording.value == false) {
                         RecordButtonViewModel.isRecording(true)
                         dotsAn.circleCountdown()
                         micIcon.hide()
                         RecordButtonViewModel.countdown()
                         onClickCallback()
                         timer.schedule(timerTask {
                             Platform.runLater {
                                 circle.animate()
                                 graphic = stopIcon
                                 stopIcon.fill = c("#CC4141")
                                 dotsAn.hide()
                                 //animationCompleted()
                             }
                         }, 3000)
                     } else {
                         if(countdown.value == "") {
                             /*if countdown.value = "" that means the countdown has finished
                             * therefore now the user is able to click stop to record
                             * */
                                circle.stop()
                                stopClickedCallback()
                         }
                     }
                 }

             }
         }

     val dotsWrapper = hbox {
         alignment = Pos.CENTER
         style {
             padding = box(20.px)
         }
         add(dotsAn)
     }

}
