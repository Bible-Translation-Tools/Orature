package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.ScopeSchema
import org.junit.Assert.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class BurritoToResourceContainerConverterProtectedTest {

    private class TestableConverter(directoryProvider: IDirectoryProvider) :
        BurritoToResourceContainerConverter(directoryProvider, mockk()) {
        fun callGroupAudioIngredientsByChapter(
            book: String,
            ingredients: List<Pair<String, IngredientSchema>>
        ) = groupAudioIngredientsByChapter(book, ingredients)

        fun callGetCompleteBookIngredients(
            book: String,
            ingredients: List<Pair<String, IngredientSchema>>
        ) = getCompleteBookIngredients(book, ingredients)

        fun callParseChapterRangeFromBibleReferences(reference: String) =
            parseChapterRangeFromBibleReferences(reference)
    }

    private fun tempDirectoryProvider(): IDirectoryProvider = mockk(relaxed = true) {
        every { tempDirectory } returns File(System.getProperty("java.io.tmpdir"))
    }

    private fun scopeFor(book: String, refs: List<String>): ScopeSchema = ScopeSchema().apply {
        this[book] = refs.toMutableList()
    }

    @Test
    fun testParseChapterRangeFromBibleReferences() {
        val converter = TestableConverter(tempDirectoryProvider())

        assertEquals(listOf(1), converter.callParseChapterRangeFromBibleReferences("1"))
        assertEquals(listOf(2, 3, 4), converter.callParseChapterRangeFromBibleReferences("2-4"))
        // Verses are ignored; only chapter portion is used
        assertEquals(listOf(3), converter.callParseChapterRangeFromBibleReferences("3:5"))
        assertEquals(listOf(3), converter.callParseChapterRangeFromBibleReferences("3:5-7"))
        // Invalid formats should return empty
        assertEquals(emptyList<Int>(), converter.callParseChapterRangeFromBibleReferences(""))
        assertEquals(emptyList<Int>(), converter.callParseChapterRangeFromBibleReferences("abc"))
    }

    @Test
    fun testGetCompleteBookIngredients() {
        val converter = TestableConverter(tempDirectoryProvider())
        val book = "GEN"

        val completeIngredient: IngredientSchema = mockk {
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf() }
        }
        val partialIngredient1: IngredientSchema = mockk {
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("1") }
        }
        val partialIngredient2: IngredientSchema = mockk {
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("2-3") }
        }

        val ingredients = listOf(
            "a.mp3" to completeIngredient,
            "b.mp3" to partialIngredient1,
            "c.mp3" to partialIngredient2
        )

        val completeOnly = converter.callGetCompleteBookIngredients(book, ingredients)
        assertEquals(listOf("a.mp3"), completeOnly.map { it.first })
    }

    @Test
    fun testGroupAudioIngredientsByChapter() {
        val converter = TestableConverter(tempDirectoryProvider())
        val book = "GEN"

        val ing1: IngredientSchema = mockk { // applies to chapter 1
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("1") }
        }
        val ing2: IngredientSchema = mockk { // applies to 1-2
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("1-2") }
        }
        val ing3: IngredientSchema = mockk { // applies to chapter 3 (verses ignored)
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("3:1-5") }
        }
        val ing4: IngredientSchema = mockk { // applies to chapter 2 only
            every { scope } returns ScopeSchema().apply { this[book] = mutableListOf("2") }
        }

        val items = listOf(
            "1.mp3" to ing1,
            "1-2.mp3" to ing2,
            "3.mp3" to ing3,
            "2.mp3" to ing4
        )

        val grouped = converter.callGroupAudioIngredientsByChapter(book, items)

        assertEquals(listOf("1.mp3", "1-2.mp3"), grouped[1]?.map { it.first })
        assertEquals(listOf("1-2.mp3", "2.mp3"), grouped[2]?.map { it.first })
        assertEquals(listOf("3.mp3"), grouped[3]?.map { it.first })
    }
}
