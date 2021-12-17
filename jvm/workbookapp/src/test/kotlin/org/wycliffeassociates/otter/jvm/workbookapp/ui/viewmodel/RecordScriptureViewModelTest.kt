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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.ReplayRelay
import java.io.File
import java.time.LocalDate
import javafx.beans.property.SimpleObjectProperty
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeHolder
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordScriptureViewModelTest {

    private val activeChunk = Chunk(
        sort = 1,
        audio = createAssociatedAudio(),
        textItem = TextItem("Chunk 1", MimeType.USFM),
        start = 1,
        end = 1,
        contentType = ContentType.TEXT,
        resources = listOf(),
        label = "Chunk"
    )

    private var selectedTake: Take?
        get() = activeChunk.audio.selected.value?.value
        set(take) {
            activeChunk.audio.selected.accept(TakeHolder(take))
        }

    var selectedTakeProperty = SimpleObjectProperty<Take?>()
    init {
        activeChunk.audio.selected.value?.value?.getProperty(RecordScriptureViewModelTest::selectedTakeProperty)
    }

    private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create<Take>())

    @Test
    fun test() {
        val take1 = Take(
            "testTake1.wav",
            File("testTake1.wav"),
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        val take2 = Take(
            "testTake2.wav",
            File("testTake2.wav"),
            2,
            MimeType.WAV,
            LocalDate.now()
        )

        selectedTakeProperty.onChangeAndDoNow {
            println("From on change: ${selectedTake?.name}")
        }

        selectedTake = take1
        println("Hardcode 1: ${selectedTake?.name}")

        selectedTake = take2
        println("Hardcode 2: ${selectedTake?.name}")
    }
}
