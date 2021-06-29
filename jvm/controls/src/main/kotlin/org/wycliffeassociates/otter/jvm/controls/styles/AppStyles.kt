/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls.styles

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.utils.images.ImageLoader
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val jfxSnackbarContent by cssclass()
        val jfxSnackbarToast by cssclass()
        val jfxSnackbarAction by cssclass()
        val backButton by cssclass()
        val appBackground by cssclass()
        val whiteBackground by cssclass()
        val appToggleButton by cssclass()

        fun recordIcon(size: String = "1em") = MaterialIconView(MaterialIcon.MIC_NONE, size)
        fun editIcon(size: String = "1em") = MaterialIconView(MaterialIcon.EDIT, size)
        fun viewTakesIcon(size: String = "1em") = MaterialIconView(MaterialIcon.APPS, size)
        fun backIcon(size: String = "1em") = MaterialIconView(MaterialIcon.ARROW_BACK, size)
        fun forwardIcon(size: String = "1em") = MaterialIconView(MaterialIcon.ARROW_FORWARD, size)
        fun bookIcon(size: String = "1em") = MaterialIconView(MaterialIcon.BOOK, size)
        fun chapterIcon(size: String = "1em") = MaterialIconView(MaterialIcon.CHROME_READER_MODE, size)
        fun verseIcon(size: String = "1em") = MaterialIconView(MaterialIcon.BOOKMARK, size)
        fun projectGraphic() = ImageLoader.load(
            ClassLoader.getSystemResourceAsStream("images/project_image.png"),
            ImageLoader.Format.PNG
        )

        fun chapterGraphic() = ImageLoader.load(
            ClassLoader.getSystemResourceAsStream("images/chapter_image.png"),
            ImageLoader.Format.PNG
        )

        fun chunkGraphic() = ImageLoader.load(
            ClassLoader.getSystemResourceAsStream("images/verse_image.png"),
            ImageLoader.Format.PNG
        )

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
            cursor = Cursor.HAND
        }

        appBackground {
            backgroundColor += AppTheme.colors.defaultBackground
        }

        whiteBackground {
            backgroundColor += AppTheme.colors.white
        }

        appToggleButton {
            textFill = AppTheme.colors.defaultText
            unsafe("-jfx-toggle-color", raw(AppTheme.colors.appRed.css))
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
