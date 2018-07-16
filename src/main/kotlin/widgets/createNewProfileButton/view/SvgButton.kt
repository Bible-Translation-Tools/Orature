package widgets.createNewProfileButton.view

import afester.javafx.svg.SvgLoader
import tornadofx.*
import javafx.scene.Group
import javafx.scene.layout.StackPane


class SvgButton(var buttonSize: Double= 64.0,
                var svgFileName: String,
                var svgScaleXCompareToButton: Double = 0.50484,
                var svgScaleYCompareToButton: Double = 0.50484): StackPane() {

    //get resource method is adapted from: https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-resource-folder
    var svgGroup = SvgLoader().loadSvg(Thread.currentThread().contextClassLoader.getResource("$svgFileName.svg").path)
    init{
        val newProfileIcon = button(graphic = svgGroup) {
            importStylesheet(SvgButtonStyle:: class)
            addClass(SvgButtonStyle.SvgIcon)
            prefWidth = buttonSize
            prefHeight = buttonSize
            resizeSvg(svgGroup, buttonSize)
            action {
                println("Go to New Profile Id Recorder phase 1 view click")
            }
        }
        add(newProfileIcon)
    }


    fun resizeSvg(svgGroup: Group, size: Double = buttonSize) {

        // adapted from https://stackoverflow.com/questions/38953921/how-to-set-the-size-of-a-svgpath-in-javafx
        val currentWidth = svgGroup.prefWidth(-1.0) // get the default preferred width
        val currentHeight = svgGroup.prefHeight(currentWidth) // get default preferred height

        //Suggested (svgScaleX/svgScaleY)CompareToButton for "Create New Profile" is (0.50484/0.2734)
        svgGroup.scaleX = (size * svgScaleXCompareToButton) / currentWidth
        svgGroup.scaleY = (size* svgScaleYCompareToButton) / currentHeight
    }
}