package org.wycliffeassociates.otter.jvm.workbookapp.ui.styles

import javafx.scene.paint.Paint
import tornadofx.*

class SplashScreenStyles : Stylesheet() {
    companion object {
        val splashRoot by cssclass()
        val splashProgress by cssclass()
    }

    init {
        splashRoot {
            splashProgress {
                bar {
                    backgroundInsets += CssBox(1.px, 1.px, 1.px, 1.px)
                    padding = CssBox(0.3.em, 0.3.em, 0.3.em, 0.3.em)
                    fill = Paint.valueOf("#015AD9")
                }
            }
        }
    }
}
