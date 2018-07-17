package com.example.demo.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RectangleButton
import widgets.RoundButton.view.RoundButton

class RecordUser : View("My View") {
    override val root = borderpane {

        fun navHome() {
            find(RecordUser::class).replaceWith(DatagridDemo::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))
        }

        val RecordButton = widgets.RoundButton.view.RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", outerCircle = true, outerCircleRadius = 120.0)
        val CloseButton = RectangleButton(width= 100.0,myFill = "#CC4141", icon = MaterialIcon.CLOSE, operation =::navHome )

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

