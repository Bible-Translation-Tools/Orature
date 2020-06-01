package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import tornadofx.*

class ResourceGroupCard(
    group: ResourceGroupCardItem,
    filterCompletedCardsProperty: BooleanProperty
) : VBox() {
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
                        resourceCardFragment(it, filterCompletedCardsProperty).root
                    )
                }
            }
        }
    }
}

fun resourcegroupcard(
    group: ResourceGroupCardItem,
    filterCompletedCardsProperty: BooleanProperty,
    init: ResourceGroupCard.() -> Unit = {}
): ResourceGroupCard {
    val rgc = ResourceGroupCard(group, filterCompletedCardsProperty)
    rgc.init()
    return rgc
}