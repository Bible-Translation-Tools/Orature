package widgets.RoundButton.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import tornadofx.*
import javafx.scene.Group
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.ArcType
import widgets.ViewMine


//Class to create circle button with MaterialIcon  inside
//Use "::functionName" to pass function as parameter
//Example how to use: RoundButton(64.0, c("#CC4141"), MaterialIcon.HOME, operation = ::abc)
//For suggestions the best values to put in the parameter for certain buttons, scroll to bottom of this file

class RoundButton(var buttonSize: Double= 64.0, var myVariable: Paint = c("#ffffff"),
                  icon: MaterialIcon, var iconSize: String = "25px",
                  fillColor: String, operation: () -> Unit,
                  var outerCircle: Boolean = false, var outerCircleRadius : Double = buttonSize - 30.0): StackPane() {

        val circle = circle {
            radius = outerCircleRadius
            fill = c("#E5E5E5") }

    init {
        val mIcon = MaterialIconView(icon, iconSize)

        val svgButton = button("", mIcon) {
            if (outerCircle) circle else circle.removeFromParent()
            importStylesheet(RoundButtonStyle::class)
            addClass(RoundButtonStyle.SvgIcon)
            prefWidth = buttonSize
            prefHeight = buttonSize
            style {
                backgroundColor += myVariable
                mIcon.fill = c(fillColor)
                cursor = Cursor.HAND

            }
            action {
                operation()
            }
        }

        add(svgButton)
    }

}

//How to use:
//
//In parent view file:
//    1. "import widgets.svgButton.view.RoundButton"
//    2. "add(RoundButton(UseOneOfTheValuesBelow))" inside the layout
//
//    For Example:
//        1. in MainView.kt:
//
//                import tornadofx.*
//                import widgets.svgButton.view.RoundButton
//
//                class MainView : View() {
//                    override val root = stackpane {
//                        add(RoundButton(64.0, c("#ffffff"), "addNewProfileIcon", 0.50484, 0.2734, operation = ::abc))
//                    }
//                }
//
//                fun abc() {
//                    println("test")
//                }


//Suggested values according to design mockup https://xd.adobe.com/spec/dd9d45e2-6d29-4524-5dc0-61fe5a4e466a-abac/

//"Create New Profile" button = RoundButton(64.0, c("#ffffff"), "addNewProfileIcon", 0.50484, 0.2734, operation = ::functionName)
//"Big Mic Icon" button = RoundButton(152.68, c("#ffffff"), "micIcon", 0.3088, 0.4192, operation = ::functionName)
//"Commit recored name" button = RoundButton(65.81, c("#CC4141"), "forwardArrowIcon", 0.2367, 0.2307, operation = ::functionName)

