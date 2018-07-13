package widgets.createNewProfileButton.view

import afester.javafx.svg.SvgLoader
import tornadofx.*
import javafx.scene.Group
import javafx.scene.layout.StackPane
import widgets.createNewProfileButton.view.CreateNewProfileButtonStyle.Companion.NewProfIcon


class CreateNewProfileButton(var buttonSize: Double= 64.0): StackPane() {
    //get resource method is adapted from: https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-resource-folder
    var svgGroup = SvgLoader().loadSvg(Thread.currentThread().contextClassLoader.getResource("addNewProfileIcon.svg").path)
    init{
        val newProfileIcon = button(graphic = svgGroup) {

            addClass(NewProfIcon)
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

        svgGroup.scaleX = (size * 0.50484) / currentWidth
        svgGroup.scaleY = (size* 0.2734) / currentHeight
    }
}