package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import tornadofx.*

class TakesListView(
    items: ObservableList<Take>,
    createTakeNode: (Take) -> Node
) : ListView<Take>(items) {
    init {
        cellFormat {
            /* Don't use cell caching, because we remove the front node of the take card when it is dragged
                and we don't ever add it back if it was made the selected take. (This is because we create a
                new take card if it was selected.)
             */
            graphic = createTakeNode(it)
        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(RecordResourceStyles.takesList)
    }
}