package org.wycliffeassociates.otter.common.domain.content

import org.junit.Test
import org.wycliffeassociates.otter.common.assertEqualsForEach
import org.wycliffeassociates.otter.common.data.model.ContentType

class FileNamerTest {
    private fun createFileNamer(chapterCount: Long, chapterTitle: String, chapterSort: Int) = FileNamer(
        chapterCount = chapterCount,
        chapterTitle = chapterTitle,
        chapterSort = chapterSort,
        start = 1,
        end = 1,
        chunkCount = 10,
        sort = 0,
        contentType = ContentType.BODY,
        languageSlug = "en",
        bookSlug = "gen",
        rcSlug = "tn"
    )

    private fun createFileNamer(start: Int?, end: Int?, chunkCount: Long) = FileNamer(
        start = start,
        end = end,
        chunkCount = chunkCount,
        sort = 0,
        contentType = ContentType.BODY,
        languageSlug = "en",
        bookSlug = "gen",
        rcSlug = "tn",
        chapterCount = 1,
        chapterTitle = "title",
        chapterSort = 1
    )

    data class FormatChapterNumberTestCase(val title: String, val sort: Int, val chapterCount: Int)
    private fun fcnCase(title: String, sort: Int, chapterCount: Int) =
        FormatChapterNumberTestCase(title, sort, chapterCount)
    @Test
    fun testFormatChapterNumber() {
        val sort = 5
        // first: Chapter.title, second: Chapter.sort, third: chapterCount
        mapOf(
            // title represents an int
            //      chapterCount < 100
            fcnCase("1", sort, 20) to "c01",
            fcnCase("4", sort, 99) to "c04",
            fcnCase("10", sort, 99) to "c10",
            //      chapterCount >= 100
            fcnCase("1", sort, 100) to "c001",
            fcnCase("10", sort, 100) to "c010",
            fcnCase("1", sort, 120) to "c001",
            fcnCase("10", sort, 120) to "c010",
            fcnCase("100", sort, 120) to "c100",
            // title does not represent an int
            //      chapterCount < 100
            fcnCase("title", 5, 20) to "c05",
            fcnCase("title", 5, 99) to "c05",
            fcnCase("title", 10, 99) to "c10",
            //      chapterCount >= 100
            fcnCase("title", 5, 100) to "c005",
            fcnCase("title", 10, 101) to "c010",
            fcnCase("title", 100, 101) to "c100"
        ).assertEqualsForEach {
            val fileNamer = createFileNamer(it.chapterCount.toLong(), it.title, it.sort)
            fileNamer.formatChapterNumber()
        }
    }

    data class FormatVerseNumberTestCase(val start: Int?, val end: Int?, val chunkCount: Int)
    private fun fvnCase(start: Int?, end: Int?, chunkCount: Int) =
        FormatVerseNumberTestCase(start, end, chunkCount)
    @Test
    fun testFormatVerseNumber() {
        // first: Recordable.start, second: Recordable.end, third: chunkCount
        mapOf(
            // start == null
            fvnCase(null, null, 10) to null,
            // start == end
            //      chunkCount < 100
            fvnCase(4, 4, 10) to "v04",
            fvnCase(4, 4, 99) to "v04",
            //      chunkCount >= 100
            fvnCase(4, 4, 100) to "v004",
            fvnCase(10, 10, 100) to "v010",
            fvnCase(4, 4, 101) to "v004",
            fvnCase(100, 100, 101) to "v100",
            // start != end
            //      chunkCount < 100
            fvnCase(4, 5, 10) to "v04-05",
            fvnCase(4, 5, 99) to "v04-05",
            fvnCase(10, 20, 99) to "v10-20",
            //      chunkCount >= 100
            fvnCase(4, 5, 100) to "v004-005",
            fvnCase(4, 25, 120) to "v004-025",
            fvnCase(10, 20, 120) to "v010-020",
            fvnCase(10, 120, 120) to "v010-120"
        ).assertEqualsForEach {
            val fileNamer = createFileNamer(it.start, it.end, it.chunkCount.toLong())
            fileNamer.formatVerseNumber()
        }
    }
}