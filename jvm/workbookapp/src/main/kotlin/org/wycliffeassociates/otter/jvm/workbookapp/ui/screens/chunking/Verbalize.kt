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

import javafx.scene.shape.Circle
import org.kordamp.ikonli.javafx.FontIcon
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.VerbalizeViewModel
import tornadofx.*

class Verbalize : View() {
    private val logger = LoggerFactory.getLogger(Verbalize::class.java)

    val chunkVm: ChunkingViewModel by inject()
    val vm: VerbalizeViewModel by inject()

    val playIcon = FontIcon("mdi-play")
    val pauseIcon = FontIcon("mdi-pause")
    val recordIcon = FontIcon("mdi-microphone")
    val stopIcon = FontIcon("mdi-stop")
    val rerecordButton = FontIcon("mdi-sync")

    val arc = Circle(120.0, 120.0, 60.0).apply {
        addClass("verbalize__animation")
    }

    override fun onDock() {
        super.onDock()
        importStylesheet(resources["/css/verbalize-page.css"])
        logger.info("Verbalize docked")
        vm.onDock()
        chunkVm.titleProperty.set(messages["verbalizeTitle"])
        chunkVm.stepProperty.set(messages["verbalizeDescription"])
    }

    override val root = borderpane {
        addClass("verbalize")
        center = hbox {
            addClass("verbalize__grouping")
            button {
                styleClass.addAll("btn", "btn--primary", "verbalize__btn--secondary")
                visibleProperty().bind(vm.hasContentProperty)
                vm.isPlayingProperty.onChangeAndDoNow {
                    it?.let {
                        when(it) {
                            true -> graphic = pauseIcon
                            false -> graphic = playIcon
                        }
                    }
                }
                action { vm.playToggle() }
            }
            stackpane {
                addClass("verbalize__action-container")
                add(arc)
                button {
                    styleClass.addAll("btn", "btn--cta", "verbalize__btn--primary")
                    vm.recordingProperty.onChangeAndDoNow {
                        it?.let {
                            when (it) {
                                true -> graphic = stopIcon
                                false -> graphic = recordIcon
                            }
                        }
                    }
                    action {
                        arc.radiusProperty().bind(vm.arcLengthProperty)
                        vm.toggle()
                    }
                }
            }
            button {
                styleClass.addAll("btn", "btn--secondary", "verbalize__btn--secondary")
                visibleProperty().bind(vm.hasContentProperty)
                graphic = rerecordButton
                action { vm.reRecord() }
            }
        }
    }
}
