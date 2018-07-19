package app.ui.widgets.profileIcon

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*


class ProfileIconStyle : Stylesheet() {
    companion object {
        val ProfileIcon by cssclass()
    }
    init {
        ProfileIcon {
            backgroundColor += c("#ffffff")
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND

            and(hover) {
                opacity = 0.90
                scaleX = 1.1
                scaleY = 1.1
            }
        }
    }
}