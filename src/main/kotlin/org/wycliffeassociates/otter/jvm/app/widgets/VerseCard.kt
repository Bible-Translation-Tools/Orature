package org.wycliffeassociates.otter.jvm.app.widgets


import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.app.ui.chapterpage.model.Verse
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class VerseCard(verse: Verse) : VBox() {
    var title = verse.verseNumber
    var hasSelectedTake = verse.hasSelectedTake
    var selectedTake = verse.selectedTakeNum
    var actionButton = Button()

    init {
        with(root) {
            alignment = Pos.CENTER
            spacing = 10.0
            label(" Verse " + title.toString())
            if (hasSelectedTake) label("Take " + selectedTake.toString())
            add(actionButton)
        }
    }
}

fun versecard(verse: Verse, init: VerseCard.() -> Unit): VerseCard {
    val vc = VerseCard(verse)
    vc.init()
    return vc
}