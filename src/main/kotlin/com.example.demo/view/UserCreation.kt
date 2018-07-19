package com.example.demo.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import tornadofx.*
import widgets.RectangleButton
import widgets.RoundButton.view.RoundButton
import widgets.RecordButton.RecordButton
import widgets.ViewMine
import app.ui.ProgressBar
import com.example.demo.ViewModel.UserCreationViewModel

class UserCreation : View("My View") {
    val UserCreationViewModel = UserCreationViewModel()


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


        val jj = RecordButton()
        val test = ProgressBar()

        top {

            hbox {
                alignment = Pos.BOTTOM_RIGHT
                add(CloseButton)
                style {
                    alignment = Pos.BOTTOM_RIGHT
                    paddingRight= 40.0
                    paddingTop = 40.0

                }
            }

        }
        center{

            add(jj)
            jj.alignment = Pos.CENTER
            RecordButton.alignment = Pos.CENTER


        }
    }
}

