package widgets.svgButton.view

import afester.javafx.svg.SvgLoader
import tornadofx.*
import javafx.scene.Group
import javafx.scene.layout.StackPane
import javafx.scene.paint.Paint


//Class to create circle button with SVG graphic inside
//Use "::functionName" to pass function as parameter
//Example how to use: SvgButton(64.0, c("#CC4141"), "forwardArrowIcon", operation = ::abc)
//For suggestions the best values to put in the parameter for certain buttons, scroll to bottom of this file
class SvgButton(var buttonSize: Double= 64.0,
                var myVariable: Paint = c("#ffffff"),
                var svgFileName: String,
                var svgScaleXCompareToButton: Double = 0.50484,
                var svgScaleYCompareToButton: Double = 0.50484,
                operation: () -> Unit ): StackPane() {

    //get resource method is adapted from: https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-resource-folder
    var svgGroup = SvgLoader().loadSvg(Thread.currentThread().contextClassLoader.getResource("$svgFileName.svg").path)

    init {
        val svgButton = button(graphic = svgGroup) {
            importStylesheet(SvgButtonStyle::class)
            addClass(SvgButtonStyle.SvgIcon)
            prefWidth = buttonSize
            prefHeight = buttonSize
            style {
                backgroundColor += myVariable
            }
            resizeSvg(svgGroup, buttonSize)
            action {
                operation()
            }
        }

        add(svgButton)
    }

    fun resizeSvg(svgGroup: Group, size: Double = buttonSize) {

        // adapted from https://stackoverflow.com/questions/38953921/how-to-set-the-size-of-a-svgpath-in-javafx
        val currentWidth = svgGroup.prefWidth(-1.0) // get the default preferred width
        val currentHeight = svgGroup.prefHeight(currentWidth) // get default preferred height


        svgGroup.scaleX = (size * svgScaleXCompareToButton) / currentWidth
        svgGroup.scaleY = (size * svgScaleYCompareToButton) / currentHeight
    }
}

//How to use:
//
//In parent view file:
//    1. "import widgets.svgButton.view.SvgButton"
//    2. "add(SvgButton(UseOneOfTheValuesBelow))" inside the layout
//
//    For Example:
//        1. in MainView.kt:
//
//                import tornadofx.*
//                import widgets.svgButton.view.SvgButton
//
//                class MainView : View() {
//                    override val root = stackpane {
//                        add(SvgButton(64.0, c("#ffffff"), "addNewProfileIcon", 0.50484, 0.2734, operation = ::abc))
//                    }
//                }
//
//                fun abc() {
//                    println("test")
//                }


//Suggested values according to design mockup https://xd.adobe.com/spec/dd9d45e2-6d29-4524-5dc0-61fe5a4e466a-abac/

//"Create New Profile" button = SvgButton(64.0, c("#ffffff"), "addNewProfileIcon", 0.50484, 0.2734, operation = ::functionName)
//"Big Mic Icon" button = SvgButton(152.68, c("#ffffff"), "micIcon", 0.3088, 0.4192, operation = ::functionName)
//"Commit recored name" button = SvgButton(65.81, c("#CC4141"), "forwardArrowIcon", 0.2367, 0.2307, operation = ::functionName)

