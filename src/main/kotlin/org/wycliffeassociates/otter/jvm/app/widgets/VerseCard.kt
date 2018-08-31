package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Verse
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class VerseCard(verse: Verse, color: String, icon: MaterialIcon, text: String, default: Boolean) : VBox() {

    init {
        with(root) {
            when (default) { //used for takes and edit contexts
                true -> {
                    when (verse.selected_take) {
                        true -> {
                            alignment = Pos.CENTER
                            spacing = 10.0
                            label("Verse " + verse.verseNumber.toString())
                            label("Take 01") { style { fontSize = 12.px } }
                            button(text, MaterialIconView(icon)) {
                                style {
                                    backgroundColor += c(color)
                                }
                            }
                        }
                        else -> {
                            alignment = Pos.CENTER
                            spacing = 25.0
                            label("Verse" + verse.verseNumber.toString())
                            style{
                                backgroundColor += c("#EDEDED")
                            }
                        }
                    }
                }

                false -> { //used for record context
                    when (verse.selected_take) {
                        true -> {
                            alignment = Pos.CENTER
                            spacing = 10.0
                            label("Verse " + verse.verseNumber.toString())
                            label("Take 01") { style { fontSize = 12.px } }
                            button(text, MaterialIconView(icon)) {
                                style {
                                    textFill = c(color)
                                    backgroundColor += Color.WHITE
                                    borderColor += box(c(color))
                                }
                            }
                        }
                        else -> {
                            alignment = Pos.CENTER
                            spacing = 25.0
                            label("Verse" + verse.verseNumber.toString())
                            button(text, MaterialIconView(icon)) {
                                style {
                                    backgroundColor += c(color)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}