package org.wycliffeassociates.otter.jvm.controls.projectnav

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.styles.AppTheme
import tornadofx.*

class ProjectNavStyles : Stylesheet() {

    companion object {
        val navbutton by cssclass()
        val navBoxInnercard by cssclass()
        val cardLabel by cssclass()
    }

    init {

        navBoxInnercard {
            backgroundColor += AppTheme.colors.lightBackground
            borderColor += box(Color.WHITE)
            borderWidth += box(3.0.px)
            borderRadius += box(5.0.px)
            borderInsets += box(1.5.px)
        }

        navbutton {
            backgroundColor += AppTheme.colors.white
            textFill = AppTheme.colors.defaultText
            borderColor += box(AppTheme.colors.lightBackground)
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            effect = DropShadow(2.0, 2.0, 2.0, AppTheme.colors.defaultBackground)
            prefWidth = 90.px
        }
        cardLabel {
            effect = DropShadow(25.0, 2.0, 2.0, c("#FBFEFF"))
            fontSize = 24.px
        }
    }
}
