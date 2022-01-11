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
package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.ReplayRelay
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.assertEqualsForEach
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import java.io.File
import java.time.LocalDate

class TestAssociatedAudio {
    private fun createTakeWithNum(num: Int): Take =
        Take(
            "name",
            File("."),
            num,
            MimeType.WAV,
            LocalDate.now()
        )

    private fun createTakesRelay(numTakes: Int): ReplayRelay<Take> {
        val takesRelay = ReplayRelay.create<Take>()
        for (i in 1..numTakes) {
            takesRelay.accept(createTakeWithNum(i))
        }
        return takesRelay
    }

    @Test
    fun testInsertTake() {
        val audio = AssociatedAudio(createTakesRelay(0))

        val take1 = createTakeWithNum(1)
        audio.insertTake(take1)
        Assert.assertEquals(1, audio.takes.values.size)
        Assert.assertTrue(audio.takes.contains(take1).blockingGet())

        val take2 = createTakeWithNum(2)
        audio.insertTake(take2)
        Assert.assertEquals(2, audio.takes.values.size)
        Assert.assertTrue(audio.takes.contains(take2).blockingGet())
    }

    @Test
    fun testGetAllTakes() {
        val numTakes = 10
        val audio = AssociatedAudio(createTakesRelay(numTakes))
        val array = audio.getAllTakes()
        Assert.assertEquals(numTakes, array.size)
    }

    @Test
    fun testGetNewTakeNumber() {
        val inputs = listOf(0, 1, 2, 9, 10, 99, 100)
        val outputs = inputs.map { it + 1 }
        inputs
            .zip(outputs)
            .toMap()
            .assertEqualsForEach {
                val audio = AssociatedAudio(createTakesRelay(it))
                audio.getNewTakeNumber().blockingGet()
            }
    }
}
