package org.wycliffeassociates.otter.jvm.controls.card

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import tornadofx.*

class ListViewPlaceHolder : ListView<Any?>() {
    init {
        addClass("wa-list-view")
        items.add(null)
        setCellFactory {
            object : ListCell<Any?>() {
                override fun updateItem(item: Any?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic = EmptyCardCell().apply {
                        addClass("card--scripture-take--empty")
                    }
                }
            }
        }
    }
}
