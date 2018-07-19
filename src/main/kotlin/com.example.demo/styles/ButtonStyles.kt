package com.example.demo.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*

class ButtonStyles: Stylesheet() {

        companion object {
            val rectangleButtonDefault by cssclass()
            val rectangleButtonAlternate by cssclass()
            val circleButton by cssclass()
            val roundButton by cssclass()
        }

        init {

            rectangleButtonDefault {
                fill = c("#CC4141")
                backgroundColor+= c("#FFFF")
                minWidth = 100.0.px
                cursor= Cursor.HAND
                effect = DropShadow(10.0, Color.GRAY)



                and(hover) {
                    scaleX = 1.1
                    scaleY = 1.1
                }

            }

            rectangleButtonAlternate {
                fill = c("#FFFF")
                backgroundColor+= c("#CC4141")
                minWidth = 100.0.px
                cursor = Cursor.HAND
                effect = DropShadow(10.0, Color.GRAY)



                and(hover) {
                    scaleX = 1.1
                    scaleY = 1.1
                }

            }

            circleButton {
                fill = c("#CC4141")
                backgroundColor+= c("#FFFF")
                cursor = Cursor.HAND
            }

            roundButton {
                backgroundRadius += box(100.percent)
                borderRadius += box(100.percent)
                effect = DropShadow(10.0, Color.GRAY)
                minWidth = 64.0.px
                minHeight = 64.0.px
                cursor = Cursor.HAND
                backgroundColor += Color.WHITE
                borderColor += box(Color.TRANSPARENT)

                and(hover) {
                    scaleX = 1.1
                    scaleY = 1.1
                }
            }

        }
    }

