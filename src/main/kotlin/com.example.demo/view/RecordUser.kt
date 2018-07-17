package com.example.demo.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RectangleButton
import widgets.svgButton.view.RoundButton

class RecordUser : View("My View") {
    override val root = borderpane {

        val RecordButton = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px")
        val CloseButton = RectangleButton(width= 100.0,myFill = "#CC4141", icon = MaterialIcon.CLOSE )

        top {
            add(CloseButton)
            style {
                CloseButton.alignment = Pos.TOP_RIGHT

            }

        }

        center{
            add(RecordButton)
        }
        style{
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
        }
    }
}

