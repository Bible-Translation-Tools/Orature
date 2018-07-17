package widgets.RoundButton.view

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*


class RoundButtonStyle: Stylesheet() {
    companion object {
        val SvgIcon by cssclass()
    }
    init {
        SvgIcon {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)

            and(hover) {
                opacity = 0.9
            }
        }
    }
}