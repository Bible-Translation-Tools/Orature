package com.example.demo.view

import com.example.demo.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*
import widgets.RecordButton.RecordButton


class UserCreation : View("My View") {
    val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")


    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        fun navHome() {
            find(UserCreation::class).replaceWith(WelcomeScreen::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))

        }

        val CloseButton = button("CLOSE",mIcon) {
            importStylesheet(ButtonStyles::class)
            addClass(ButtonStyles.rectangleButtonDefault)

            style {
                alignment = Pos.CENTER
                mIcon.fill = c("#CC4141")
                effect = DropShadow(10.0, Color.GRAY)

            }
            action {
                navHome()
            }
        }

        val recordButton = RecordButton()

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

            add(recordButton)
            recordButton.alignment = Pos.CENTER

        }
    }
}

