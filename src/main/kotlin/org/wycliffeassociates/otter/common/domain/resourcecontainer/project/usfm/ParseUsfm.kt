package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm

import java.io.Reader

private val sep = System.lineSeparator()

private const val MARKER_BOOK_NAME = "\\id"
private const val MARKER_CHAPTER_NUMBER = "\\c"
private const val MARKER_VERSE_NUMBER = "\\v"
private const val MARKER_NEW_PARAGRAPH = "\\p"
private const val MARKER_SECTION_HEADING = "\\s"
private const val MARKER_SECTION_HEADING_ONE = "\\s1"
private const val MARKER_SECTION_HEADING_TWO = "\\s2"
private const val MARKER_SECTION_HEADING_THREE = "\\s3"
private const val MARKER_SECTION_HEADING_FOUR = "\\s4"
private const val MARKER_CHUNK = "\\s5"

data class Current(var c: Int = 1, var v: Int = 1)
data class Verse(val number: Int, var text: String)

class UsfmDocument : HashMap<Int, HashMap<Int, Verse>>()

class ParseUsfm(val reader: Reader) {
    val chapters: UsfmDocument = UsfmDocument()

    fun parse(): ParseUsfm {
        val fileCursor = Current()
        reader.use {
            it.forEachLine {
                parseLine(it, fileCursor)
            }
        }
        return this
    }

    private fun parseLine(line: String, current: Current) {
        val split = line.split("\\s+".toRegex(), 2)
        if (split.isEmpty()) {
            return
        }
        when (split[0]) {
            MARKER_BOOK_NAME -> return
            MARKER_CHAPTER_NUMBER -> {
                current.c = split[1]
                    ?.replace("\\s".toRegex(), "")
                    .toInt() // strip potential whitespace and convert to int
                chapters[current.c] = hashMapOf()
            }
            MARKER_VERSE_NUMBER -> {
                val sub = split[1].split("\\s+".toRegex(), 2)
                // Check for verse bridges
                val numbers = sub[0].replace("\\s".toRegex(), "")
                    .split("-")
                    .map { it.toInt() }
                // Add all the verses
                // Verse text is ignored since
                // 1) it is not needed, 2) the current parser cannot extract text from word tags, if present
                for (verseNumber in numbers[0]..(numbers.getOrNull(1) ?: numbers[0])) {
                    current.v = verseNumber
                    chapters[current.c]!![current.v] = Verse(current.v, "")
                }
            }
            MARKER_NEW_PARAGRAPH -> return
            MARKER_SECTION_HEADING -> return
            MARKER_SECTION_HEADING_ONE -> return
            MARKER_SECTION_HEADING_TWO -> return
            MARKER_SECTION_HEADING_THREE -> return
            MARKER_SECTION_HEADING_FOUR -> return
            MARKER_CHUNK -> return
            "" -> return
            // catch styling or formatting
            else -> {
                if (split[0].length == 1) {
                    // add this to the next coming verse
                    // addFormattingToNextVerse(line)
                } else {
                    // add this to the last verse
                    addFormattingToVerse(line, current)
                }
            }
        }
    }

    private fun addFormattingToVerse(line: String, verse: Current) {
        if (chapters.containsKey(verse.c) && chapters[verse.c]!!.containsKey(verse.v)) {
            chapters[verse.c]!![verse.v]!!.text += "$sep $line"
        }
    }
}