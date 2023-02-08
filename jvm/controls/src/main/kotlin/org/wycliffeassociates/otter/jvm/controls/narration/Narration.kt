package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.primitives.Verse
import tornadofx.*

class Narration(verses: ObservableList<Verse>) : VBox() {
    init {
        stackpane {
            addClass("narration__recorder")
            alignment = Pos.CENTER

            vbox {
                addClass("narration__recorder-tip")
                alignment = Pos.CENTER_LEFT

                label("Tip") {
                    addClass("narration__recorder-tip-title")
                    style = "-fx-font-weight: bold;"
                }
                label("Press the down key on your keyboard to navigate to the next verse.")
            }
        }
        listview(verses) {
            addClass("wa-list-view")
            alignment = Pos.TOP_CENTER

            setCellFactory { NarrationVerseListCell() }
            selectionModel.select(0)
        }
    }
}