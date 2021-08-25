/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
