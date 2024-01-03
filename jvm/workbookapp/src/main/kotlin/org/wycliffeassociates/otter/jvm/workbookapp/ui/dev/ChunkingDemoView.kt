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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.ChunkingStepsDrawer
import tornadofx.*

class ChunkingDemoView : View() {

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(2)
    private val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    private val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val list = observableListOf<ChunkViewData>()

    override val root = vbox {
        borderpane {
            left = ChunkingStepsDrawer(selectedStepProperty).apply {
                chunksProperty.setAll(list)
                this.reachableStepProperty.bind(this@ChunkingDemoView.reachableStepProperty)
            }
            center= Label("Fragment here").addClass("h4")

            right = SourceTextDrawer().apply {
                textProperty.set("1. Source text verse 1 here\n2. Verse two text here")
            }
        }

    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/source-content.css")
    }
}