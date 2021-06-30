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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.utils.images.ImageLoader
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.RecordResourceStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.RecordScriptureStyles
import tornadofx.*

class TakeCardStyles : Stylesheet() {

    companion object {
        val scriptureTakeCard by cssclass()
        val scriptureTakeCardPlaceholder by cssclass()
        val resourceTakeCard by cssclass()
        val resourceTakeCardPlaceholder by cssclass()
        val badge by cssclass()
        val iconStyle by cssclass()
        val content by cssclass()
        val takeNumberLabel by cssclass()
        val timestampLabel by cssclass()
        val defaultButton by cssclass()
        val takeProgressBar by cssclass()
        val editButton by cssclass()
        val topHalf by cssclass()
        val scriptureTakeCardDropShadow by cssclass()
        val defaultGreen: Color = c("#58BD2F")
        val grey = c("#C9C8C8")

        private fun scriptureTakeWidthHeight() = RecordScriptureStyles.takeWidthHeight()
        private fun scriptureTakeRadius() = RecordScriptureStyles.takeRadius()
        private fun resourceTakeWidthHeight() = RecordResourceStyles.takeWidthHeight()
        private fun resourceTakeRadius() = RecordResourceStyles.takeRadius()

        fun draggingIcon() = ImageLoader.load(
            ClassLoader.getSystemResourceAsStream("images/baseline-drag_indicator-24px.svg"),
            ImageLoader.Format.SVG
        )
    }

    init {
        takeNumberLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }

        takeProgressBar {
            track {
                backgroundColor += Color.LIGHTGRAY
                backgroundRadius += box(5.0.px)
            }
            bar {
                backgroundColor += c("#0094F0")
                backgroundRadius += box(5.0.px)
            }
        }

        // RESOURCE TAKE CARD specific styles
        resourceTakeCard {
            +resourceTakeWidthHeight()
            +resourceTakeRadius()
            backgroundColor += AppTheme.colors.cardBackground
            borderColor += box(grey)
            topHalf {
                padding = box(4.px, 5.px, 5.px, 5.px)
                borderColor += box(Color.TRANSPARENT, Color.TRANSPARENT, c("C9C8C8"), Color.TRANSPARENT)
                borderWidth += box(1.px)
            }
            takeProgressBar {
                track {
                    minHeight = 30.px
                }
                bar {
                    minHeight = 30.px
                }
            }
        }

        resourceTakeCardPlaceholder {
            +resourceTakeWidthHeight()
            +resourceTakeRadius()
            backgroundColor += AppTheme.colors.defaultBackground
            borderColor += box(grey)
            borderWidth += box(1.px)
        }

        // SCRIPTURE TAKE CARD specific styles
        scriptureTakeCard {
            +scriptureTakeWidthHeight()
            borderRadius += box(5.px)
            borderColor += box(AppTheme.colors.imagePlaceholder)
            borderWidth += box(1.px)
            backgroundColor += AppTheme.colors.cardBackground
            label {
                textFill = AppTheme.colors.defaultText
            }
            backgroundRadius += box(5.px)
            badge {
                backgroundColor += AppTheme.colors.appRed
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
                graphicTextGap = 7.5.px
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
                effect = DropShadow(1.0, 2.0, 2.0, Color.LIGHTGRAY)
                backgroundColor += Color.WHITE
            }

            editButton {
                textFill = TakeCardStyles.defaultGreen
            }

            takeProgressBar {
                track {
                    minHeight = 40.px
                }
                bar {
                    minHeight = 40.px
                }
            }
        }

        scriptureTakeCardPlaceholder {
            +scriptureTakeWidthHeight()
            +scriptureTakeRadius()
            backgroundColor += AppTheme.colors.defaultBackground
        }

        scriptureTakeCardDropShadow {
            effect = DropShadow(2.0, 2.0, 2.0, AppTheme.colors.dropShadow)
        }
    }
}
