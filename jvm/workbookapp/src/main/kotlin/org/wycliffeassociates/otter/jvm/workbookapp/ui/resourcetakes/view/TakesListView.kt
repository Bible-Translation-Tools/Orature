package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel
import tornadofx.*

class TakesListView(
    items: ObservableList<TakeCardModel>,
    createTakeNode: (TakeCardModel) -> Node
) : ListView<TakeCardModel>(items) {
    init {
        cellFormat {
            /* Don't use cell caching, because we remove the front node of the take card when it is dragged
                and we don't ever add it back if it was made the selected take. (This is because we create a
                new take card if it was selected.)
             */
            if (!it.selected) {
                graphic = createTakeNode(it)
            }
        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(RecordResourceStyles.takesList)
    }
}
