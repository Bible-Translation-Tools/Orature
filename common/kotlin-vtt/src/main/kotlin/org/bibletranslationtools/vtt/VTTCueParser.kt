package org.bibletranslationtools.vtt

import org.bibletranslationtools.vtt.WebvttParserUtil.parseTimestampUs
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min


val CUE_HEADER_PATTERN: Pattern = Pattern.compile("^(\\S+)\\s+-->\\s+(\\S+)(.*)?$")
private val CUE_SETTING_PATTERN: Pattern = Pattern.compile("(\\S+?):(\\S+)")

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

private val TAG = "WebvttCueParser"

object VTTCueParser {
    fun parseCue(webvttData: ParsableByteArray): WebvttCueInfo? {
        val firstLine: String = webvttData.readLine() ?: return null
        var cueHeaderMatcher: Matcher = CUE_HEADER_PATTERN.matcher(firstLine)
        if (cueHeaderMatcher.matches()) {
            // We have found the timestamps in the first line. No id present.
            return parseCue(
                null,
                cueHeaderMatcher,
                webvttData,
            )
        }
        // The first line is not the timestamps, but could be the cue id.
        val secondLine: String = webvttData.readLine() ?: return null
        cueHeaderMatcher = CUE_HEADER_PATTERN.matcher(secondLine)
        if (cueHeaderMatcher.matches()) {
            // We can do the rest of the parsing, including the id.
            return parseCue(
                firstLine.trim { it <= ' ' },
                cueHeaderMatcher,
                webvttData
            )
        }
        return null
    }

    // Internal methods
    private fun parseCue(
        id: String?,
        cueHeaderMatcher: Matcher,
        webvttData: ParsableByteArray,
    ): WebvttCueInfo? {
        val builder = WebvttCueInfoBuilder()
        try {
            // Parse the cue start and end times.
            builder.startTimeUs = parseTimestampUs(checkNotNull(cueHeaderMatcher.group(1)))
            builder.endTimeUs = parseTimestampUs(checkNotNull(cueHeaderMatcher.group(2)))
        } catch (e: NumberFormatException) {
            // Log.w("Skipping cue with bad header: " + cueHeaderMatcher.group())
            return null
        }

        parseCueSettingsList(checkNotNull(cueHeaderMatcher.group(3)), builder)

        // Parse the cue text.
        val textBuilder = StringBuilder()
        var line: String? = webvttData.readLine()
        while (line?.isNotEmpty() == true) {
            if (textBuilder.isNotEmpty()) {
                textBuilder.append("\n")
            }
            textBuilder.append(line.trim { it <= ' ' })
            line = webvttData.readLine()
        }
        builder.text = parseCueText(id, textBuilder.toString())
        builder.markup = textBuilder.toString()
        return builder.build()
    }

    private fun parseCueSettingsList(cueSettingsList: String, builder: WebvttCueInfoBuilder) {
        // Parse the cue settings list.
        val cueSettingMatcher: Matcher = CUE_SETTING_PATTERN.matcher(cueSettingsList)

        while (cueSettingMatcher.find()) {
            val name: String = checkNotNull(cueSettingMatcher.group(1))
            val value: String = checkNotNull(cueSettingMatcher.group(2))
        }
    }

    fun parseCueText(id: String?, markup: String): String {
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
                        continue
                    }
                    if (isClosingTag) {
                        var startTag: StartTag
                        do {
                            if (startTagStack.isEmpty()) {
                                break
                            }
                            startTag = startTagStack.pop()
                            if (!startTagStack.isEmpty()) {
                                nestedElements.add(
                                    Element(
                                        startTag,
                                        spannedText.length
                                    )
                                )
                            } else {
                                nestedElements.clear()
                            }
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

        return spannedText.toString()
    }


    private class WebvttCueInfoBuilder {
        var startTimeUs: Long = 0
        var endTimeUs: Long = 0

        var text: CharSequence? = null
        var markup: CharSequence? = null

        fun build(): WebvttCueInfo {
            val cue = toCueBuilder().build()
            return WebvttCueInfo(cue, startTimeUs, endTimeUs)
        }

        fun toCueBuilder(): Cue.Builder {
            val cueBuilder: Cue.Builder = Cue.Builder()

            if (text != null) {
                cueBuilder.setText(text)
            }

            markup?.let{
                cueBuilder.addMarkup(it)
            }

            return cueBuilder
        }
    }
}

object IdGen {
    private var id: Int = 0
    fun getId(): Int = id++
}

data class WebvttCueInfo(
    val cue: Cue,
    val startTimeUs: Long,
    val endTimeUs: Long
) {
    val id: Int = IdGen.getId()
}

fun findEndOfTag(markup: String, startPos: Int): Int {
    val index: Int = markup.indexOf(CHAR_GREATER_THAN, startPos)
    return if (index == -1) markup.length else index + 1
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
    private val startTag: StartTag,
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