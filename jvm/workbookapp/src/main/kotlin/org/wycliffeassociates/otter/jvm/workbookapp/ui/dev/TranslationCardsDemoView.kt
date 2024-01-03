/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.card.translationCard
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class TranslationCardsDemoView : View() {

    val languages = listOf(
        Language("en", "English", "English", "", true, ""),
        Language("fr", "français", "French", "", true, ""),
    )

    init {
        tryImportStylesheet("/css/translation-card-2.css")
    }

    private val showNewTranslationCard = SimpleBooleanProperty(false)

    override val root = vbox {
        spacing = 10.0
        paddingAll = 20.0
        maxWidth = 300.0

        translationCard(
            languages[0],
            languages[1],
            ProjectMode.TRANSLATION
        )

        newTranslationCard(
            SimpleObjectProperty<Language>(
                languages[0]
            ),
            SimpleObjectProperty<Language>(null),
            mode = SimpleObjectProperty<ProjectMode>()
        ) {
            visibleWhen(showNewTranslationCard)
            managedWhen(visibleProperty())

            setOnCancelAction {
                showNewTranslationCard.set(false)
            }
        }
        translationCreationCard {
            visibleWhen(showNewTranslationCard.not())
            managedWhen(visibleProperty())

            setOnAction {
                showNewTranslationCard.set(true)
            }
        }
    }
}