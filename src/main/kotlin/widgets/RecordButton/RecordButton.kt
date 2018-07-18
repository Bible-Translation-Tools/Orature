package widgets.RecordButton

import com.example.demo.ViewModel.UserCreationViewModel
import com.github.thomasnield.rxkotlinfx.toObservable
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.value.ObservableStringValue
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RoundButton.view.RoundButton
import widgets.RoundButton.view.RoundButtonStyle
import widgets.ViewMine
import java.util.*
import kotlin.concurrent.timerTask


class RecordButton : VBox() {


    val circle = ViewMine()
    val dotsAn = DotsAnimation()
    val UserCreationViewModel = UserCreationViewModel()

    val countdown = UserCreationViewModel.countdownTracker

    // val micButton = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", myVariable = c("#FFFF"))
    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "65px")
    val micButton = button(countdown, micIcon){
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
    }

    val stopIcon = MaterialIconView(MaterialIcon.STOP, "65px")
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
             timer.schedule(timerTask { circle.animate() }, 3000)
             timer.schedule(timerTask { Platform.runLater(Runnable() {
                 run {
                     micButton.graphic = stopIcon
                     stopIcon.fill=c("#CC4141")
                 }
             }) }, 3000)

         }

    }
   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       add(dotsAn)
   }

}
