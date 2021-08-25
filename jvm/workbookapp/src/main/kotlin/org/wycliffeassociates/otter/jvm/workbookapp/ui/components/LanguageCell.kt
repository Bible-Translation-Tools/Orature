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

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.card.LanguageCardCell
import tornadofx.*
import java.util.concurrent.Callable

enum class LanguageType {
    SOURCE,
    TARGET
}

class LanguageCell(
    private val type: LanguageType,
    private val anglicisedProperty: BooleanProperty,
    private val existingLanguages: ObservableList<Language> = observableListOf(),
    private val onSelected: (Language) -> Unit
) : ListCell<Language>() {

    private val view = LanguageCardCell()

    override fun updateItem(item: Language?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            iconProperty.value = when (type) {
                LanguageType.SOURCE -> FontIcon(Material.HEARING)
                LanguageType.TARGET -> FontIcon(MaterialDesign.MDI_VOICE)
            }

            languageNameProperty.bind(anglicisedProperty.stringBinding {
                it?.let {
                    when {
                        it && item.anglicizedName.isNotBlank() -> item.anglicizedName
                        else -> item.name
                    }
                }
            })
            languageSlugProperty.set(item.slug)

            setOnMouseClicked {
                onSelected(item)
            }

            disableProperty().bind(Bindings.createBooleanBinding(
                Callable {
                    existingLanguages.contains(item)
                },
                existingLanguages
            ))
        }
    }
}
