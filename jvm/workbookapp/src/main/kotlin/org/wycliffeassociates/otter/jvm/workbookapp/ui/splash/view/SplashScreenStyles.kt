package org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.*

class SplashScreenStyles : Stylesheet() {
    companion object {
        val splashRoot by cssclass()
        val splashProgress by cssclass()
        val splashLabel by cssclass()
    }

    init {
        splashRoot {
            backgroundColor += AppTheme.colors.defaultBackground
            backgroundRadius += box(5.px)
            prefWidth = 300.px
            prefHeight = 125.px
            padding = box(20.px)
            alignment = Pos.CENTER_LEFT
            spacing = 20.px
            splashLabel {
                textFill = AppTheme.colors.defaultText
                fontSize = 2.em
            }
            splashProgress {
                maxWidth = Double.MAX_VALUE.px
                track {
                    backgroundColor += AppTheme.colors.base
                }
                bar {
                    padding = box(2.px)
                    backgroundInsets += box(0.px)
                    accentColor = AppTheme.colors.appRed
                    backgroundRadius += box(0.px)
                }
            }
        }
    }
}