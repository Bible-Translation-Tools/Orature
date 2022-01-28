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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.ViewModel
import tornadofx.onChange

const val ACTIVE = "chunking-wizard__step--active"
const val COMPLETE = "chunking-wizard__step--complete"
const val INACTIVE = "chunking-wizard__step--inactive"

class ChunkingViewModel: ViewModel() {

    val consumeStepColor = SimpleStringProperty(ACTIVE)
    val verbalizeStepColor = SimpleStringProperty(INACTIVE)
    val chunkStepColor = SimpleStringProperty(INACTIVE)

    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio = SimpleObjectProperty<WavFile>()

    init {
        titleProperty.onChange {
            when(it) {
                "Consume" -> {
                    consumeStepColor.set(ACTIVE)
                    verbalizeStepColor.set(INACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                "Verbalize" -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(ACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                "Chunking" -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(COMPLETE)
                    chunkStepColor.set(ACTIVE)
                }
            }
        }
    }
}
