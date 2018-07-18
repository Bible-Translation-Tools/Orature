package widgets.RecordButton


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


     var numberText = ""



     val circle = ViewMine()

    val dotsAn = DotsAnimation()
    //val micButton = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", myVariable = c("#FFFF"))
     var icon =   MaterialIcon.MIC_NONE

    var mIcon = MaterialIconView(icon, "65px")

    val micButton = button(numberText, mIcon) {
        //if (outerCircle) circle else circle.removeFromParent()
        importStylesheet(RoundButtonStyle::class)
        addClass(RoundButtonStyle.RoundButton)
        prefWidth = 152.68
        prefHeight = 152.68
        text= numberText
        style {
            backgroundColor += c("#FFFF")
            mIcon.fill = c("#CC4141")
            cursor = Cursor.HAND
            fontSize = 6.em
            textFill = c("#CC4141")

        }
        action {

        }


    }


     val root = button {
         style {
             backgroundColor+= Color.TRANSPARENT
         }

        alignment = Pos.CENTER
         stackpane {

                 add(circle)
                 add(micButton)

         }


         action{

             mIcon.hide()
             Platform.runLater { micButton.text = "3" }
             dotsWrapper.show()
             dotsAn.showCircles()


             var timer = Timer()



             timer.schedule(timerTask {


                 Platform.runLater { micButton.text = "2" }
             }, 1000)
             timer.schedule(timerTask {


                 Platform.runLater { micButton.text = "1" }
                 println(numberText)
             }, 2000)


             timer.schedule(timerTask {

                 var icon =   MaterialIcon.STOP

                 var mIcon = MaterialIconView(icon, "75px")
                 mIcon.fill = c("#CC4141")


                 Platform.runLater { micButton.graphic = mIcon
                     micButton.text = ""

                 }
                 circle.animate()

             }, 3000)






         }



    }



   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       add(dotsAn)
   }






}
