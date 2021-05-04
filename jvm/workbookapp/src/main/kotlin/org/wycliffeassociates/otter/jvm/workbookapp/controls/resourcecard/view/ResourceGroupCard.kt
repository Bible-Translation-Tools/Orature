package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import tornadofx.*

class ResourceGroupCard(
    group: ResourceGroupCardItem,
    filterCompletedCardsProperty: BooleanProperty,
    navigator: NavigationMediator
) : VBox() {

    private val logger = LoggerFactory.getLogger(ResourceGroupCard::class.java)

    companion object {
        const val RENDER_BATCH_SIZE = 10
    }

    init {
        importStylesheet<ResourceGroupCardStyles>()

        addClass(ResourceGroupCardStyles.resourceGroupCard)
        label(group.title)

        group.resources
            .buffer(RENDER_BATCH_SIZE)
            .doOnError { e ->
                logger.error("Error in rendering resource groups", e)
            }
            .subscribe { items ->
                Platform.runLater {
                    items.forEach {
                        add(
                            resourceCardFragment(it, filterCompletedCardsProperty, navigator).root
                        )
                    }
                }
            }
    }
}

fun resourcegroupcard(
    group: ResourceGroupCardItem,
    filterCompletedCardsProperty: BooleanProperty,
    navigator: NavigationMediator,
    init: ResourceGroupCard.() -> Unit = {}
): ResourceGroupCard {
    val rgc = ResourceGroupCard(group, filterCompletedCardsProperty, navigator)
    rgc.init()
    return rgc
}
