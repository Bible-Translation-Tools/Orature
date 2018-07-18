package com.example.demo.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.shape.Arc
import tornadofx.*
import widgets.RectangleButton
import widgets.RoundButton.view.RoundButton
import widgets.RecordButton.RecordButton

class UserCreation : View("My View") {
    override val root = borderpane {

        fun navHome() {
            find(UserCreation::class).replaceWith(DatagridDemo::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))
        }

        fun animation(arc: Arc) {
            timeline {
                keyframe(javafx.util.Duration.millis(3000.0)) {
                    keyvalue(arc.lengthProperty(),-360.0)
                }
            }
        }

        val RecordButtons = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", outerCircle = true, outerCircleRadius = 120.0)
        val CloseButton = RectangleButton(width= 100.0,myFill = "#CC4141", icon = MaterialIcon.CLOSE, operation =::navHome )

        val jj = RecordButton()

        top {
            CloseButton.alignment = Pos.BOTTOM_RIGHT
            add(CloseButton)
            style {
                setPrefHeight(200.0)
                alignment = Pos.BOTTOM_RIGHT


            }

        }
        center{
            add(jj)
            jj.alignment = Pos.CENTER






        }







    }
}

