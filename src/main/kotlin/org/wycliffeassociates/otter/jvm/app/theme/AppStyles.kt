package org.wycliffeassociates.otter.jvm.app.theme

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.ProgressDialogStyles
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val jfxSnackbarContent by cssclass()
        val jfxSnackbarToast by cssclass()
        val jfxSnackbarAction by cssclass()
        val backButton by cssclass()
        val appBackground by cssclass()
        val progressDialog by cssclass()

        // Icons
        fun recordIcon(size: String = "1em") = MaterialIconView(MaterialIcon.MIC_NONE, size)
        fun editIcon(size: String = "1em") = MaterialIconView(MaterialIcon.EDIT, size)
        fun viewTakesIcon(size: String = "1em") = MaterialIconView(MaterialIcon.APPS, size)
        fun backIcon() = MaterialIconView(MaterialIcon.ARROW_BACK)
        fun chapterIcon(size: String = "1em") = MaterialIconView(MaterialIcon.CHROME_READER_MODE, size)
        fun forwardIcon(size: String ="1em") = MaterialIconView(MaterialIcon.ARROW_FORWARD)

        const val defaultFontSize = 10.0
    }

    init {
        jfxSnackbarContent {
            backgroundColor += Color.BLACK
            fontSize = 16.px
            backgroundRadius += box(5.px)
        }
        jfxSnackbarToast {
            textFill = Color.WHITE
        }
        jfxSnackbarAction {
            textFill = AppTheme.colors.appRed
            fontWeight = FontWeight.BOLD
            cursor = Cursor.HAND
        }
        // Material design scroll bar
        scrollBar {
            backgroundColor += Color.TRANSPARENT
            padding = box(0.px, 4.px)
            prefWidth = 16.px
            Stylesheet.thumb {
                backgroundColor += Color.DARKGRAY
                backgroundRadius += box(10.px)
            }
            Stylesheet.incrementArrow {
                visibility = FXVisibility.COLLAPSE
            }
            Stylesheet.decrementArrow {
                visibility = FXVisibility.COLLAPSE
            }
        }

        scrollPane {
            backgroundColor += Color.TRANSPARENT
        }

        button {
            and(backButton) {
                minWidth = 230.px
                textFill = AppTheme.colors.white
                child("*") {
                    fill = AppTheme.colors.white
                }
                backgroundColor += AppTheme.colors.appRed
                unsafe("-jfx-button-type", raw("RAISED"))
            }
        }

        appBackground {
            backgroundColor += AppTheme.colors.defaultBackground
        }

        progressDialog {
            ProgressDialogStyles.progressGraphic {
                fill = AppTheme.colors.defaultText
            }
            backgroundColor += AppTheme.colors.base
            progressIndicator {
                progressColor = AppTheme.colors.defaultText
            }

            label {
                textFill = AppTheme.colors.defaultText
            }
        }


        // Load the fonts
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Regular.ttf"), defaultFontSize)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Bold.ttf"), defaultFontSize)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-BoldItalic.ttf"), defaultFontSize)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Italic.ttf"), defaultFontSize)
        root {
            font = Font.font("Noto Sans", defaultFontSize)
            fontWeight = FontWeight.NORMAL
        }
    }
}
