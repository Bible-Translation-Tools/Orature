package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model

import com.sun.javafx.collections.ObservableListWrapper
import javafx.collections.ListChangeListener

class ResourceGroupCardItemList(list: List<ResourceGroupCardItem>) :
    ObservableListWrapper<ResourceGroupCardItem>(list) {

    init {
        addListener(
            ListChangeListener<ResourceGroupCardItem> {
                while(it.next()) {
                    if (it.wasRemoved()) {
                        it.removed.forEach { item ->
                            item.onRemove()
                        }
                    }
                }
            }
        )
    }
}