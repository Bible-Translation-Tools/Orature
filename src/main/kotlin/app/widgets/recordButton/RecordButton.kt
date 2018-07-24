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
import javafx.scene.control.Button
import java.util.*
import kotlin.concurrent.timerTask

class RecordButton() : VBox() {


    val circle = RecordingAnimation()
    val dotsAn = DotsAnimation()
    val RecordButtonViewModel = RecordButtonViewModel()
    val countdown = RecordButtonViewModel.countdownTracker



    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")
    lateinit var recordButton: Button

     val wrapper =
         stackpane {
             alignment = Pos.CENTER

             add(circle)

             recordButton= button(countdown, micIcon){
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
                     RecordButtonViewModel.countdown()
//                     RecordButtonViewModel.recording.setValue(true)
                     var timer = Timer()
                     timer.schedule(timerTask { Platform.runLater {
                         circle.animate()
                         graphic = stopIcon
                         stopIcon.fill=c("#CC4141")
                         dotsAn.hide()

                    } }, 3000)
                 }
             }

         }


   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       style{
           padding= box(20.px)
       }
       add(dotsAn)
   }


}
