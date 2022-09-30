/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingWizardPage
import tornadofx.*

class Verbalize : View() {
    private val logger = LoggerFactory.getLogger(Verbalize::class.java)

    private val chunkVm: ChunkingViewModel by inject()

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources["/css/verbalize-page.css"])
        logger.info("Verbalize docked")
        chunkVm.pageProperty.set(ChunkingWizardPage.VERBALIZE)
        chunkVm.titleProperty.set(messages["verbalizeTitle"])
        chunkVm.stepProperty.set(messages["verbalizeDescription"])
    }

    override val root = borderpane {
        addClass("verbalize")
        center = hbox {
            addClass("verbalize__grouping")
            stackpane {
                addClass("verbalize__action-container")
                button {
                    styleClass.addAll(
                        "btn", "btn--icon", "btn--borderless", "verbalize__btn"
                    )
                    graphic = FontIcon(MaterialDesign.MDI_VOICE)
                    isMouseTransparent = true
                    isFocusTraversable = false
                }
            }
        }
    }
}
