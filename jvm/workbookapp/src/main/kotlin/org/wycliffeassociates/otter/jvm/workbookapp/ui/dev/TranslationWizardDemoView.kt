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

import javafx.scene.Parent
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.card.translationTypeCard
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class TranslationWizardDemoView : View() {

    init {
        tryImportStylesheet("/css/translation-card-2.css")
    }

    override val root = vbox {
        paddingAll = 20.0
        maxWidth = 800.0

        vbox {
            addClass("translation-wizard__main")
            vgrow = Priority.ALWAYS

            translationTypeCard("oralTranslation", "oralTranslationDesc")
            translationTypeCard("narration", "narrationDesc")
            translationTypeCard("dialect", "dialectDesc") {
                addPseudoClass("last")
            }
        }
    }
}