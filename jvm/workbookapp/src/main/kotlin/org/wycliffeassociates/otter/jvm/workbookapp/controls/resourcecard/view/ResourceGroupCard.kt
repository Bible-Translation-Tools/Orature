package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import tornadofx.*

class ResourceGroupCard(group: ResourceGroupCardItem, isFilterOnProperty: SimpleBooleanProperty) : VBox() {
    companion object {
        const val RENDER_BATCH_SIZE = 10
    }

    init {
        importStylesheet<ResourceGroupCardStyles>()

        addClass(ResourceGroupCardStyles.resourceGroupCard)
        label(group.title)

        group.resources.buffer(RENDER_BATCH_SIZE).subscribe { items ->
            Platform.runLater {
                items.forEach {
                    add(
                        resourceCardFragment(it, isFilterOnProperty).root
                    )
                }
            }
        }
    }
}

fun resourcegroupcard(
    group: ResourceGroupCardItem,
    isFilterOnProperty: SimpleBooleanProperty,
    init: ResourceGroupCard.() -> Unit = {}): ResourceGroupCard {
    val rgc = ResourceGroupCard(group, isFilterOnProperty)
    rgc.init()
    return rgc
}