package widgets.RecordButton

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RoundButton.view.RoundButton
import widgets.ViewMine


class RecordButton : VBox() {



     val circle = ViewMine()

    val dotsAn = DotsAnimation()
     val micButton = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", myVariable = c("#FFFF"))

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
             circle.animate()
             dotsWrapper.show()
             dotsAn.showCircles()

         }


    }



   val dotsWrapper = hbox {
       alignment = Pos.CENTER
       add(dotsAn)
   }





}
