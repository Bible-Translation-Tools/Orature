package app.ui

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*

class ButtonStyles: Stylesheet() {
    companion object {
        val createNewProfile by cssclass()
        val bigCenter by cssclass()
        val commitRecordedName by cssclass()
    }
    init {
        createNewProfile {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)
            prefWidth =  64.0.px
            prefHeight = 64.0.px
            backgroundColor += c("#ffffff")
            and(hover) {
                opacity = 0.9
            }
        }
        bigCenter {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)
            prefWidth = 152.68.px
            prefHeight = 152.68.px
            backgroundColor += c("#ffffff")
            and(hover) {
                opacity = 0.9
            }
        }
        commitRecordedName {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)
            prefWidth = 65.81.px
            prefHeight = 65.81.px
            backgroundColor += c("#CC4141")
            and(hover) {
                opacity = 0.9
            }
        }
    }
}