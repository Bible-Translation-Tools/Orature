package com.example.demo.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*

//a temporary substitute for the users' icons, just a circle
class UserIconWidget(rad: Double): Fragment() {
    override val root = hbox {
        circle {
            radius = rad;
            fill = Color.TRANSPARENT;
        }
    }
}

//purple rectangle, temporary home button
class HomeWidget: HBox() {
    init {
        rectangle {
                style {
                    borderRadius += box(25.0.px)
                    backgroundRadius += box(25.0.px)
                }
            fill = Color.valueOf("#CC4141")
            width = 150.0
            height = 25.0

        }
    }
}

class PlusWidget: Fragment() {
    override val root = button {
        label("+");
    }
}