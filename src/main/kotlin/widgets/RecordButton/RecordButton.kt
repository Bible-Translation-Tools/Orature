package widgets.RecordButton

import com.example.demo.ViewModel.UserCreationViewModel
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RoundButton.view.RoundButtonStyle
import widgets.ViewMine
import java.util.*
import kotlin.concurrent.timerTask


class RecordButton : VBox() {


    val circle = ViewMine()
    val dotsAn = DotsAnimation()
    val UserCreationViewModel = UserCreationViewModel()
    val countdown = UserCreationViewModel.countdownTracker
    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "65px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "65px")

     val wrapper =
         stackpane {
             alignment = Pos.CENTER

             add(circle)
             button(countdown, micIcon){
                 importStylesheet(RoundButtonStyle::class)
                 addClass(RoundButtonStyle.RoundButton)
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
                         circle.animate()
                         graphic = stopIcon
                         stopIcon.fill=c("#CC4141")

                     } }, 3000)

                     timer.schedule(timerTask {
                         Platform.runLater {
                             //find(UserCreation().root).replaceWith(ProgressBar(), transition = ViewTransition.Fade(.2.seconds))

                         }
                     }, 6100)
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
