package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class TakeCardStyles : Stylesheet() {
    companion object {
        val defaultTakeCard by cssclass()
        val badge by cssclass()
        val iconStyle by cssclass()
        val content by cssclass()
        val takeNumberLabel by cssclass()
        val timestampLabel by cssclass()

        fun playIcon() = MaterialIconView(MaterialIcon.PLAY_CIRCLE_OUTLINE, "30px")
        fun pauseIcon() = MaterialIconView(MaterialIcon.PAUSE_CIRCLE_OUTLINE, "30px")
        fun deleteIcon() = MaterialIconView(MaterialIcon.DELETE, "25px")
        fun badgeIcon() = MaterialDesignIconView(MaterialDesignIcon.CREATION, "18px")
    }

    init {
        defaultTakeCard {
            minWidth = 348.px
            maxWidth = minWidth
            minHeight = 200.px
            maxHeight = minHeight
            backgroundRadius += box(10.px)
            badge {
                backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
                padding = box(8.px)
                iconStyle {
                    fill = Color.WHITE
                }
            }
            padding = box(0.px)
            content {
                padding = box(10.px)
            }
            takeNumberLabel {
                fontSize = 20.px
            }
            timestampLabel {
                fontSize = 12.px
            }
            button {
                backgroundColor += Color.TRANSPARENT
            }
        }
    }
}