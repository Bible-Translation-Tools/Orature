package com.example.demo.view

import javafx.collections.FXCollections.fill
import javafx.scene.paint.Color
import tornadofx.*

class TopWidget(rad: Double): Fragment() {
    override val root = hbox {
        circle {
            radius = rad;
            fill = Color.CORAL;
        }
    }
}

class BottomWidget: Fragment() {
    override val root = hbox {
        rectangle {
            fill = Color.DARKORCHID
            width = 50.0
            height = 25.0
        }
    }
}