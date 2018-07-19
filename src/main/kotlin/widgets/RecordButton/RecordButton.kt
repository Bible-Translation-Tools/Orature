package widgets.RecordButton

import com.example.demo.ViewModel.UserCreationViewModel
import com.example.demo.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import widgets.ViewMine
import java.util.*
import kotlin.concurrent.timerTask

class RecordButton : VBox() {


    val circle = ViewMine()
    val dotsAn = DotsAnimation()
    val UserCreationViewModel = UserCreationViewModel()

    val countdown = UserCreationViewModel.countdownTracker

    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val micButton = button(countdown, micIcon){
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
    }

    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")
    val root = button {


         style {
             backgroundColor+= Color.TRANSPARENT
         }

        alignment = Pos.CENTER
         stackpane {

             add(circle)
             add(micButton)

         }

         action {
             dotsAn.showCircles()
             micIcon.hide()
             var timer = Timer()
             UserCreationViewModel.countdown()
             timer.schedule(timerTask { Platform.runLater(Runnable() {
                 run {
                     micButton.graphic = stopIcon
                     circle.animate()
                     stopIcon.fill=c("#CC4141")
                     dotsAn.invisible()
                 }
             }) }, 3000)

         }

    }
   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       add(dotsAn)
   }

}
