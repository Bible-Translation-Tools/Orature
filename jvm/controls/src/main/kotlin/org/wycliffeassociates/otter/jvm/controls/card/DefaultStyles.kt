package org.wycliffeassociates.otter.jvm.controls.card

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.controls.styles.AppTheme
import tornadofx.*

class DefaultStyles : Stylesheet() {
    private val defaultRed = c("#CC4141")
    private val defaultWhite = c("#FFFF")
    private val defaultGray = c("#E6E8E9")
    private val defaultBlue = c("#0094F0")
    private val defaultGreen = c("#58BD2F")
    private val black = c("#000")

    companion object {
        val defaultBaseTop by cssclass()
        val baseBottom by cssclass()
        val defaultInnerCard by cssclass()
        val defaultCard by cssclass()
        val defaultCardButton by cssclass()
        val defaultCardProgressBar by cssclass()
        val activeBaseTop by cssclass()
        val completeBaseTop by cssclass()
        val defaultTitle by cssclass()
        val defaultBody by cssclass()
        val defaultMajorLabel by cssclass()
        val defaultMinorLabel by cssclass()
        val completedProgress by cssclass()

        fun checkCircle(size: String = "1em") = MaterialIconView(MaterialIcon.CHECK_CIRCLE, size)
        fun green() = c("58bd2f")
    }

    init {
        defaultBaseTop {
            prefWidth = Double.MAX_VALUE.px
            prefHeight = 70.px
            maxHeight = 70.px
            backgroundRadius += box(0.0.px, 0.0.px, 25.0.px, 25.0.px)
            backgroundColor += defaultGray
        }

        activeBaseTop {
            backgroundColor += defaultRed
        }

        completeBaseTop {
            backgroundColor += defaultGreen
        }

        baseBottom {
            backgroundColor += defaultWhite
            backgroundRadius += box(5.px)
            prefHeight = 192.px
            prefWidth = 158.px
            maxHeight = 192.px
            maxWidth = 158.px
        }

        defaultInnerCard {
            maxHeight = 118.px
            maxWidth = 142.px
            backgroundColor += Color.LIGHTGRAY
            borderColor += box(Color.WHITE)
            borderWidth += box(3.0.px)
            borderRadius += box(5.0.px)
            borderInsets += box(1.5.px)
            backgroundRadius += box(5.0.px)
            padding = box(2.0.px)
        }

        defaultCard {
            backgroundColor += Color.WHITE
            prefHeight = 192.px
            prefWidth = 158.px
            maxWidth = 158.px
            borderRadius += box(5.px)
            backgroundRadius += box(5.px)
            spacing = 10.px
            effect = DropShadow(2.0, 4.0, 6.0, AppTheme.colors.lightBackground)
        }

        defaultCardButton {
            alignment = Pos.CENTER
            maxHeight = 40.px
            maxWidth = 168.px
            borderColor += box(AppTheme.colors.appRed)
            borderRadius += box(5.0.px)
            backgroundColor += defaultWhite
            textFill = defaultRed
            cursor = Cursor.HAND
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }

        defaultCardProgressBar {
            maxWidth = 118.px
            track {
                backgroundColor += AppTheme.colors.base
            }
            bar {
                padding = box(4.px)
                backgroundInsets += box(0.px)
                accentColor = defaultBlue
                backgroundRadius += box(0.px)
            }
        }

        completedProgress {
            bar {
                accentColor = defaultGreen
            }
        }
        defaultTitle {
            fontSize = 16.px
            textFill = AppTheme.colors.defaultText
        }
        defaultBody {
            fontSize = 32.px
            fontWeight = FontWeight.BOLD
            textFill = AppTheme.colors.defaultText
        }

        defaultMajorLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
            textFill = AppTheme.colors.white
            backgroundColor += c("#000", 0.1)
        }
        defaultMinorLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
            textFill = AppTheme.colors.white
            backgroundColor += c("#000", 0.1)
        }
    }
}