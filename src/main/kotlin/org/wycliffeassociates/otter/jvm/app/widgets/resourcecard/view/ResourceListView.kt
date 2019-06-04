package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItem
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.styles.ResourceListStyles
import tornadofx.*

class ResourceListView(items: ObservableList<ResourceGroupCardItem>): ListView<ResourceGroupCardItem>(items) {
    init {
        cellFormat {
            graphic = cache(it.title) {
                resourcegroupcard(it)
            }
        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(ResourceListStyles.resourceGroupList)
    }
}