package org.wycliffeassociates.otter.common.domain.usfm

import java.io.File

private val sep = System.lineSeparator()

private val MARKER_BOOK_NAME = "\\id"
private val MARKER_CHAPTER_NUMBER = "\\c"
private val MARKER_VERSE_NUMBER = "\\v"
private val MARKER_NEW_PARAGRAPH = "\\p"
private val MARKER_SECTION_HEADING = "\\s"
private val MARKER_SECTION_HEADING_ONE = "\\s1"
private val MARKER_SECTION_HEADING_TWO = "\\s2"
private val MARKER_SECTION_HEADING_THREE = "\\s3"
private val MARKER_SECTION_HEADING_FOUR = "\\s4"
private val MARKER_CHUNK = "\\s5"


data class Current(var c: Int = 1, var v: Int = 1)
data class Verse(val number: Int, var text: String)

class UsfmDocument : HashMap<Int, HashMap<Int, Verse>>()

class ParseUsfm(val file: File) {
    val current = Current()
    val chapters: UsfmDocument = UsfmDocument()

    fun parse(): ParseUsfm {
        val reader = file.bufferedReader()
        reader.use {
            it.forEachLine {
                parseLine(it)
            }
        }
        return this
    }

    private fun parseLine(line: String) {
        val split = line.split("\\s+".toRegex(), 2)
        if (split.isEmpty()) {
            return
        }
        when (split[0]) {
            MARKER_BOOK_NAME -> return
            MARKER_CHAPTER_NUMBER -> {
                current.c = split[1]?.let {
                    it.replace("\\s".toRegex(), "").toInt() //strip potential whitespace and convert to int
                }
                chapters[current.c] = hashMapOf()
            }
            MARKER_VERSE_NUMBER -> {
                val sub = split[1].split("\\s+".toRegex(), 2)
                if (sub.size >= 2) {
                    current.v = sub[0].replace("\\s".toRegex(), "").toInt()
                    val verse = sub[1]
                    //list initialized on chapter tag parse
                    chapters[current.c]!![current.v] = Verse(current.v, verse)
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
                    //addFormattingToNextVerse(line)
                } else {
                    // add this to the last verse
                    addFormattingToLastVerse(line)
                }
            }
        }


    }

    private fun addFormattingToLastVerse(line: String) {
        if (chapters.containsKey(current.c) && chapters[current.c]!!.containsKey(current.v)) {
            chapters[current.c]!![current.v]!!.text += "$sep $line"
        }
    }
}