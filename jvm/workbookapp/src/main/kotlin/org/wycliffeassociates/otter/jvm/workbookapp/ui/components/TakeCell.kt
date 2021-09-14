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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import org.wycliffeassociates.otter.jvm.controls.card.events.AnimatedListCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel

class TakeCell(
    private val onTakeSelected: (TakeModel) -> Unit
) : AnimatedListCell<TakeModel>() {
    override val view = TakeItem()

    override fun updateItem(item: TakeModel?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }
        graphic = view.apply {
            selectedProperty.set(item.selected)
            takeProperty.set(item)

            setOnTakeSelected {
                if (isAnimatingProperty.value || item.selected) {
                    return@setOnTakeSelected
                }

                isAnimatingProperty.set(true)
                view.styleClass.add("selected")

                animate(item) {
                    view.isAnimatingProperty.set(false)
                    view.styleClass.remove("selected")
                    onTakeSelected(item)
                }
            }
        }
    }
}
