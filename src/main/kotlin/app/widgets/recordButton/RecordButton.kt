package app.widgets.recordButton


import app.ui.profilePreview.View.ProfilePreview
import app.ui.styles.ButtonStyles
import app.ui.userCreation.UserCreation
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*


import java.util.*
import kotlin.concurrent.timerTask

class RecordButton(onClickListener: () -> Unit,animationCompleted : () -> Unit, stopClicked: () -> Unit) : VBox() {


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
                         onClickListener()
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
                             stopClicked()
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
