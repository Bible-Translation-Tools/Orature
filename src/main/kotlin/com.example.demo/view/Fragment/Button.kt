package com.example.demo.view.Fragment

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*
import javax.xml.soap.Node

class ButtonComponent(color: String, width: Double, myFill : String): HBox() {

    val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")

    val root = button("",homeIcon) {
        style {
            backgroundColor+= c(color)
            setMinWidth(width)
            alignment = Pos.CENTER
            homeIcon.fill = c(myFill)

        }

    }

    init {
        alignment = Pos.CENTER
    }


}