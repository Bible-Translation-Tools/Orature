package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Verse
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class VerseCard : VBox() {
    var title: String = ""
    var selectedTake: Int = 0
    var buttonColor: Color = c("")

    init {
        with(root) {
            alignment = Pos.CENTER
            spacing = 10.0
            label(title)
            label(selectedTake.toString())
            button{
                style {
                    backgroundColor += buttonColor
                }
            }
        }
    }
}

fun versecard(init: VerseCard.() -> Unit): VerseCard {
    val vc = VerseCard()
    vc.init()
    return vc
}