package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class BiblicalReferencesTest {

    data class TestData(
        val reference: String,
        val oratureCreatedReference: String,
        val markerLabel: String,
        val marker: AudioMarker
    )

    val biblicalReferences = listOf(
        TestData("GEN 0", "GEN 0", "orature-book-gen", BookMarker("gen", 0)),
        TestData("GEN 1:0", "GEN 1:0",  "orature-chapter-1", ChapterMarker(1, 0)),
        TestData("GEN 1", "GEN 1:0", "orature-chapter-1", ChapterMarker(1, 0)),
        TestData("GEN 1:1", "GEN 1:1", "orature-vm-1", VerseMarker(1, 1, 0)),
        TestData("GEN 1:2-4", "GEN 1:2-4", "orature-vm-2-4", VerseMarker(2, 4, 0))
    )

    val bookSlug = "gen"
    val chapter = 1

    @Test
    fun testReferenceToMarker() {
        biblicalReferences.forEach { (reference, _, marker, _) ->
            Assert.assertEquals(
                BiblicalReferencesParser.parseBiblicalReference(reference),
                marker
            )
        }
    }

    @Test
    fun testMarkerToReference() {
        biblicalReferences.forEach { (reference, expected, label, marker) ->
            Assert.assertEquals(
                OratureMarkerConverter.toBiblicalReference(
                    marker,
                    bookSlug,
                    chapter
                ),
                expected
            )
        }
    }
}