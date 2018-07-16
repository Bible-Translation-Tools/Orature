package com.example.demo.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.VBox

//a temporary substitute for the users' icons, just a circle
class UserIconWidget(rad: Double): Fragment() {
    override val root = hbox {
        circle {
            radius = rad
            fill = Color.TRANSPARENT
        }
    }
}


class HomeWidget(color: String) : HBox() {
    val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px").setStyleClass("primary")

    val root = button("",homeIcon) {
        style {
            backgroundColor+=c(color)
            setMinWidth(150.0)
            alignment = Pos.CENTER

        }

    }

    init {
       alignment = Pos.CENTER
    }

}

class PlusWidget: VBox() {

    val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "50px").setStyleClass("primary")

     val root = button ("", addUserIcon){
        style {
            borderRadius+=box(50.0.px)
            backgroundColor+=c("#FFFFFF")
            backgroundRadius+=box(50.0.px)
            setMinWidth(75.0)
            setMinHeight(75.0)
            accentColor=Color.WHITE
            baseColor=Color.WHITE
            textFill=c("#CC4141")
        }
    }
}