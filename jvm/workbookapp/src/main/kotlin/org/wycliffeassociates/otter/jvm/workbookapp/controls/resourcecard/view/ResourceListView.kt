package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles.ResourceListStyles
import tornadofx.*

class ResourceListView(
    items: ObservableList<ResourceGroupCardItem>,
    filterCompletedCardsProperty: BooleanProperty
) : ListView<ResourceGroupCardItem>(items) {
    init {
        cellFormat {
            graphic = cache(it.title) {
                resourcegroupcard(it, filterCompletedCardsProperty)
            }
        }

        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(ResourceListStyles.resourceGroupList)
    }
}