package widgets.RoundButton.view

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*


class RoundButtonStyle: Stylesheet() {
    companion object {
        val RoundButton by cssclass()
    }
    init {
        RoundButton {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)
            minWidth = 64.0.px
            minHeight = 64.0.px

            and(hover) {
                opacity = 0.9
            }
        }
    }
}