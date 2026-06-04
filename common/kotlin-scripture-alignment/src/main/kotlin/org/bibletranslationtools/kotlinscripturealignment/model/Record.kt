package org.bibletranslationtools.kotlinscripturealignment.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
 import org.bibletranslationtools.vtt.Cue
 import org.bibletranslationtools.vtt.WebVttCue
 import org.bibletranslationtools.vtt.WebVttDocument
import org.bibletranslationtools.vtt.WebvttCueInfo
import org.bibletranslationtools.vtt.WebvttParserUtil.parseTimestampUs
import java.util.regex.Matcher
import java.util.regex.Pattern

class Record(
    @JsonProperty("timecode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val timecode: List<String>? = null,

    @JsonProperty("text-reference")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val textReference: List<String>? = null,

    @JsonProperty("references")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val references: List<List<String>> = listOf()
) {
    fun toWebVttCueContent(roles: List<String>?): WebVttDocument.WebVttCueContent? {
        val rawTimestamp: String?
        val rawReference: String?

        // Prioritize direct fields if available
        rawTimestamp = this.timecode?.firstOrNull()
        rawReference = this.textReference?.firstOrNull()

        if (rawTimestamp == null || rawReference == null) {
            // Fallback to references list if direct fields are not present
            val timecodeIndex = roles?.indexOf("timecode") ?: -1
            val textReferenceIndex = roles?.indexOf("text-reference") ?: -1

            if (timecodeIndex == -1 || textReferenceIndex == -1) {
                return null // Roles not found, cannot determine which reference is which
            }
            val timestampFromRefs = references.getOrNull(timecodeIndex)?.firstOrNull()
            val referenceFromRefs = references.getOrNull(textReferenceIndex)?.firstOrNull()
            if (timestampFromRefs == null || referenceFromRefs == null) {
                return null
            }
            return parseAndCreateCueContent(timestampFromRefs, referenceFromRefs)
        } else {
            return parseAndCreateCueContent(rawTimestamp, rawReference)
        }
    }

    private fun parseAndCreateCueContent(timestamp: String, reference: String): WebVttDocument.WebVttCueContent? {
        val cue = Cue.Builder().build()
        var cueHeaderMatcher: Matcher = Pattern.compile("^(\\S+)\\s+-->\\s+(\\S+)(.*)?$").matcher(timestamp)
        try {
            cueHeaderMatcher.matches()
            // Parse the cue start and end times.
            val startTimeUs = parseTimestampUs(checkNotNull(cueHeaderMatcher.group(1)))
            val endTimeUs = parseTimestampUs(checkNotNull(cueHeaderMatcher.group(2)))

            val wvc = WebVttCue(WebvttCueInfo(cue, startTimeUs, endTimeUs))
            return WebVttDocument.WebVttCueContent(reference, reference, wvc)
        } catch (e: NumberFormatException) {
            return null
        }
    }
}
