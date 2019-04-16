package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItem
import tornadofx.*

class ResourceGroupCard(group: ResourceGroupCardItem) : VBox() {
    init {
        importStylesheet<ResourceGroupCardStyles>()

        addClass(ResourceGroupCardStyles.resourceGroupCard)
        label(group.title)
        group.resources.subscribe {
            add(
                resourcecard(it)
            )
        }
    }
}

fun resourcegroupcard(group: ResourceGroupCardItem, init: ResourceGroupCard.() -> Unit = {}): ResourceGroupCard {
    val rgc = ResourceGroupCard(group)
    rgc.init()
    return rgc
}