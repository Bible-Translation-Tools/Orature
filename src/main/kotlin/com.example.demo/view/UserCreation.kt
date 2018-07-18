package com.example.demo.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import tornadofx.*
import widgets.RectangleButton
import widgets.RoundButton.view.RoundButton
import widgets.ViewMine
import widgets.RecordButton.RecordButton

class UserCreation : View("My View") {
    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        fun navHome() {
            find(UserCreation::class).replaceWith(WelcomeScreen::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))

        }


//        val RecordButtons = RoundButton(buttonSize = 152.68, fillColor = "#CC4141", icon = MaterialIcon.MIC_NONE, operation = ::println, iconSize = "65px", outerCircle = true, outerCircleRadius = 120.0)
        val CloseButton = RectangleButton(width= 100.0,myFill = "#CC4141", icon = MaterialIcon.CLOSE, operation =::navHome )
        val anim = ViewMine()
        val RecordButton = RecordButton()


        top {

            hbox {
                alignment = Pos.BOTTOM_RIGHT
                add(CloseButton)
                style {
                    setPrefHeight(200.0)
                    alignment = Pos.BOTTOM_RIGHT
                    padding= box(40.0.px)


                }
            }

        }
        center{
            add(RecordButton)
            RecordButton.alignment = Pos.CENTER


        }



    }
}

