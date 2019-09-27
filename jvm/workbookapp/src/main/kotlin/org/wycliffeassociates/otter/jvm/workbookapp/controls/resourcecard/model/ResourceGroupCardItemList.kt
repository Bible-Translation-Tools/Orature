package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model

import com.sun.javafx.collections.ObservableListWrapper
import javafx.collections.ListChangeListener

class ResourceGroupCardItemList : ObservableListWrapper<ResourceGroupCardItem>(mutableListOf()) {
    init {
        addListener(
            ListChangeListener<ResourceGroupCardItem> {
                while (it.next()) {
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