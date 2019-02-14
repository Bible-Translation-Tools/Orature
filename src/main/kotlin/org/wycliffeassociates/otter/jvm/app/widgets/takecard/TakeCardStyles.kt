package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import tornadofx.*

class TakeCardStyles : Stylesheet() {
    companion object {
        val defaultTakeCard by cssclass()
        val badge by cssclass()
        val iconStyle by cssclass()
        val content by cssclass()
        val takeNumberLabel by cssclass()
        val timestampLabel by cssclass()
        val defaultButton by cssclass()
        val defaultGreen : Color = c("#58BD2F")

        fun playIcon() = MaterialIconView(MaterialIcon.PLAY_CIRCLE_OUTLINE, "30px")
        fun pauseIcon() = MaterialIconView(MaterialIcon.PAUSE_CIRCLE_OUTLINE, "30px")
        fun deleteIcon() = MaterialIconView(MaterialIcon.DELETE, "25px")
        fun badgeIcon() = MaterialDesignIconView(MaterialDesignIcon.CREATION, "18px")
        fun draggingIcon() = ImageLoader.load(
                ClassLoader.getSystemResourceAsStream("images/baseline-drag_indicator-24px.svg"),
                ImageLoader.Format.SVG
        )
    }

    init {
        defaultTakeCard {
            minWidth = 348.px
            maxWidth = minWidth
            minHeight = 200.px
            maxHeight = minHeight
            backgroundRadius += box(5.px)
            badge {
                backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
                padding = box(8.px)
                iconStyle {
                    fill = Color.WHITE
                }
            }
            padding = box(5.px)
            content {
                padding = box(10.px)
            }
            takeNumberLabel {
                fontSize = 16.px
                graphicTextGap = 7.5.px
                fontWeight = FontWeight.BOLD
            }
            timestampLabel {
                fontSize = 12.px
                fontWeight = FontWeight.LIGHT
                fontStyle = FontPosture.ITALIC
                textFill = Color.LIGHTGRAY
                padding = box(2.5.px)
            }
            button {
                backgroundColor += Color.TRANSPARENT
            }
            defaultButton {
                minHeight = 40.px
                minWidth = 150.px
                borderRadius += box(5.0.px)
                backgroundRadius += box(5.0.px)
                borderColor += box(Color.LIGHTGRAY)
                borderWidth += box(0.5.px)
                effect = DropShadow(1.0,2.0,2.0,Color.LIGHTGRAY)
                backgroundColor += Color.WHITE
            }
        }
    }
}