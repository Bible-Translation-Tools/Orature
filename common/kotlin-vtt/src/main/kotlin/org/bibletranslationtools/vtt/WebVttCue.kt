package org.bibletranslationtools.vtt

import java.util.ArrayDeque
import java.util.ArrayList
import java.util.HashSet
import kotlin.math.min


private val CHAR_LESS_THAN = '<'
private val CHAR_GREATER_THAN = '>'
private val CHAR_SLASH = '/'
private val CHAR_AMPERSAND = '&'
private val CHAR_SEMI_COLON = ';'
private val CHAR_SPACE = ' '

private val ENTITY_LESS_THAN = "lt"
private val ENTITY_GREATER_THAN = "gt"
private val ENTITY_AMPERSAND = "amp"
private val ENTITY_NON_BREAK_SPACE = "nbsp"

private val TAG_BOLD = "b"
private val TAG_CLASS = "c"
private val TAG_ITALIC = "i"
private val TAG_LANG = "lang"
private val TAG_RUBY = "ruby"
private val TAG_RUBY_TEXT = "rt"
private val TAG_UNDERLINE = "u"
private val TAG_VOICE = "v"

class WebVttCue(cueInfo: WebvttCueInfo) {

    internal val classes = mutableMapOf<String, MutableList<String>>()

    var text: String = cueInfo.cue.text.toString()
    var startTimeUs: Long = cueInfo.startTimeUs
    var endTimeUs: Long = cueInfo.endTimeUs

    init {
        cueInfo.cue.content.forEach {
            val elements: MutableList<Element> = mutableListOf()
            it.lines().forEach {
                elements.addAll(parseCueText(null, it))
            }
            elements.forEach { elem ->
                val tag = elem.startTag.name
                if (!classes.containsKey(tag)) {
                    classes[tag] = mutableListOf()
                }

                classes[tag]!!.add(elem.content)
            }
        }
    }

    fun getContentForTag(tag: String): List<String> {
        return classes[tag] ?: listOf()
    }

    private fun parseCueText(id: String?, markup: String): List<Element> {
        val spannedText = StringBuilder()
        val startTagStack: ArrayDeque<StartTag> = ArrayDeque<StartTag>()
        var pos = 0
        val nestedElements: MutableList<Element> = ArrayList<Element>()
        while (pos < markup.length) {
            val curr = markup[pos]
            when (curr) {
                CHAR_LESS_THAN -> {
                    if (pos + 1 >= markup.length) {
                        pos++
                        break // avoid ArrayOutOfBoundsException
                    }
                    val ltPos = pos
                    val isClosingTag = markup[ltPos + 1] == CHAR_SLASH
                    pos = findEndOfTag(markup, ltPos + 1)
                    val isVoidTag = markup[pos - 2] == CHAR_SLASH
                    val fullTagExpression =
                        markup.substring(ltPos + (if (isClosingTag) 2 else 1), if (isVoidTag) pos - 2 else pos - 1)
                    if (fullTagExpression.trim { it <= ' ' }.isEmpty()) {
                        continue
                    }
                    val tagName: String = getTagName(fullTagExpression)
                    if (!isSupportedTag(tagName)) {
                        break
                    }
                    if (isClosingTag) {
                        var startTag: StartTag
                        do {
                            if (startTagStack.isEmpty()) {
                                break
                            }
                            startTag = startTagStack.pop()
                            nestedElements.add(
                                Element(
                                    startTag,
                                    spannedText.toString(),
                                    spannedText.length
                                )
                            )
                        } while (startTag.name != tagName)
                    } else if (!isVoidTag) {
                        startTagStack.push(
                            StartTag.buildStartTag(
                                fullTagExpression,
                                spannedText.length
                            )
                        )
                    }
                }

                CHAR_AMPERSAND -> {
                    val semiColonEndIndex: Int =
                        markup.indexOf(CHAR_SEMI_COLON, pos + 1)
                    val spaceEndIndex: Int =
                        markup.indexOf(CHAR_SPACE, pos + 1)
                    val entityEndIndex: Int =
                        if (semiColonEndIndex == -1) spaceEndIndex
                        else (if (spaceEndIndex == -1) semiColonEndIndex
                        else min(semiColonEndIndex.toDouble(), spaceEndIndex.toDouble()).toInt())
                    if (entityEndIndex != -1) {
                        if (entityEndIndex == spaceEndIndex) {
                            spannedText.append(" ")
                        }
                        pos = entityEndIndex + 1
                    } else {
                        spannedText.append(curr)
                        pos++
                    }
                }

                else -> {
                    spannedText.append(curr)
                    pos++
                }
            }
        }
        if (nestedElements.isEmpty()) {
            nestedElements.add(
                Element(
                    StartTag.buildStartTag("text", 0),
                    markup.trimStart('-').strip(),
                    markup.length
                )
            )
        }

        return nestedElements
    }

    /**
     * Returns the tag name for the given tag contents.
     *
     * @param tagExpression Characters between &amp;lt: and &amp;gt; of a start or end tag.
     * @return The name of tag.
     */
    private fun getTagName(tagExpression: String): String {
        var tagExpression = tagExpression
        tagExpression = tagExpression.trim { it <= ' ' }
        checkNotNull(tagExpression.isNotEmpty())
        return splitAtFirst(tagExpression, "[ \\.]")[0]
    }

    private fun isSupportedTag(tagName: String): Boolean {
        return when (tagName) {
            TAG_BOLD,
            TAG_CLASS,
            TAG_ITALIC,
            TAG_LANG,
            TAG_RUBY,
            TAG_RUBY_TEXT,
            TAG_UNDERLINE,
            TAG_VOICE -> true

            else -> false
        }
    }


    private class StartTag private constructor(
        val name: String,
        val position: Int,
        val voice: String,
        val classes: Set<String>
    ) {
        companion object {
            fun buildStartTag(fullTagExpression: String, position: Int): StartTag {
                var fullTagExpression = fullTagExpression
                fullTagExpression = fullTagExpression.trim { it <= ' ' }
                check(fullTagExpression.isNotEmpty())
                val voiceStartIndex = fullTagExpression.indexOf(" ")
                val voice: String
                if (voiceStartIndex == -1) {
                    voice = ""
                } else {
                    voice = fullTagExpression.substring(voiceStartIndex).trim { it <= ' ' }
                    fullTagExpression = fullTagExpression.substring(0, voiceStartIndex)
                }
                val nameAndClasses: Array<String> = fullTagExpression.split("\\.").toTypedArray()
                val name = nameAndClasses[0]
                val classes: MutableSet<String> = HashSet()
                for (i in 1 until nameAndClasses.size) {
                    classes.add(nameAndClasses[i])
                }
                return StartTag(name, position, voice, classes)
            }

            fun buildWholeCueVirtualTag(): StartTag {
                return StartTag( /* name= */
                    "",  /* position= */
                    0,  /* voice= */
                    "",  /* classes= */
                    emptySet()
                )
            }
        }
    }

    /** Information about a complete element (i.e. start tag and end position).  */
    private class Element constructor(
        val startTag: StartTag,
        val content: String,
        /**
         * The position of the end of this element's text in the un-marked-up cue text (i.e. the
         * corollary to [position]).
         */
        private val endPosition: Int
    ) {
        companion object {
            private val BY_START_POSITION_ASC =
                Comparator { e1: Element, e2: Element ->
                    Integer.compare(
                        e1.startTag.position,
                        e2.startTag.position
                    )
                }
        }
    }
}