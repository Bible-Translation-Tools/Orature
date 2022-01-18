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

class ChunkingViewModel: ViewModel() {

    private val green = Paint.valueOf("#1edd76")
    private val gray = Paint.valueOf("#0a337333")
    private val blue = Paint.valueOf("#015ad9")

    val consumeStepColor = SimpleObjectProperty(Paint.valueOf("#0a337333"))
    val verbalizeStepColor = SimpleObjectProperty(Paint.valueOf("#0a337333"))
    val chunkStepColor = SimpleObjectProperty(Paint.valueOf("#0a337333"))

    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio = SimpleObjectProperty<WavFile>()

    init {
        titleProperty.onChange {
            when(it) {
                "Consume" -> {
                    consumeStepColor.set(blue)
                    verbalizeStepColor.set(gray)
                    chunkStepColor.set(gray)
                }
                "Verbalize" -> {
                    consumeStepColor.set(green)
                    verbalizeStepColor.set(blue)
                    chunkStepColor.set(gray)
                }
                "Chunking" -> {
                    consumeStepColor.set(green)
                    verbalizeStepColor.set(green)
                    chunkStepColor.set(blue)
                }
            }
        }
    }
}
