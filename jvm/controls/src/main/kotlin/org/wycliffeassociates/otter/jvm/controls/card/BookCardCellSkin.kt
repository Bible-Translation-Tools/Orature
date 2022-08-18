/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.card

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.io.File

class BookCardCellSkin(private val cell: BookCardCell) : SkinBase<BookCardCell>(cell) {
    private val behavior = ButtonBehavior(cell)
    private val graphicRadius = 15.0

    init {
        children.addAll(
            HBox().apply {
                addClass("book-card-cell__root")
                stackpane {
                    addClass("book-card-cell__graphic")
                    hbox {
                        addClass("book-card-cell__bg")
                        label {
                            graphic = FontIcon(MaterialDesign.MDI_BOOK)
                        }
                    }
                    region {
                        backgroundProperty().bind(
                            cell.coverArtProperty.objectBinding {
                                it?.let { Background(backgroundImage(it.file)) }
                            }
                        )
                        val rect = Rectangle().apply {
                            widthProperty().bind(this@region.widthProperty())
                            heightProperty().bind(this@region.heightProperty())
                            arcWidth = graphicRadius
                            arcHeight = graphicRadius
                        }
                        clip = rect
                        tooltip {
                            textProperty().bind(cell.attributionProperty)
                        }
                    }
                }

                vbox {
                    addClass("book-card-cell__title")
                    label(cell.bookNameProperty).apply {
                        addClass("book-card-cell__name")
                    }
                    hbox {
                        addClass("book-card-cell__info")
                        label(cell.bookSlugProperty)
                        label {
                            addClass("book-card-cell__dot")
                            fitToParentHeight()
                            graphic = FontIcon(MaterialDesign.MDI_CHECKBOX_BLANK_CIRCLE)
                        }
                        label(cell.licenseProperty)
                    }
                }
            }
        )
    }

    private fun backgroundImage(file: File): BackgroundImage {
        val url = file.toURI().toURL().toExternalForm()
        val image = Image(url, true)
        val backgroundSize = BackgroundSize(
            BackgroundSize.AUTO,
            BackgroundSize.AUTO,
            true,
            true,
            false,
            true
        )
        return BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            backgroundSize
        )
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
