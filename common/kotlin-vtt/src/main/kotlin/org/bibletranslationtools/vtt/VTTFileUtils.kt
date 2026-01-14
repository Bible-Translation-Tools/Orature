package org.bibletranslationtools.vtt

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Special constant representing an unset or unknown time or duration. Suitable for use in any
 * time base.
 */
private val TIME_UNSET: Long = Long.MIN_VALUE + 1

/** Represents an unset or unknown index or byte position.  */
val INDEX_UNSET: Int = -1

object WebvttParserUtil {


    private val COMMENT: Pattern = Pattern.compile("^NOTE([ \t].*)?$")
    private const val WEBVTT_HEADER = "WEBVTT"

    /**
     * Reads and validates the first line of a WebVTT file.
     *
     * @param input The input from which the line should be read.
     * @throws ParserException If the line isn't the start of a valid WebVTT file.
     */
    fun validateWebvttHeaderLine(input: ParsableByteArray) {
        val startPosition = input.getPosition()
        if (!isWebvttHeaderLine(input)) {
            input.setPosition(startPosition)
            throw Exception(
                "Expected WEBVTT. Got " + input.readLine(),  /* cause= */null
            )
        }
    }

    /**
     * Returns whether the given input is the first line of a WebVTT file.
     *
     * @param input The input from which the line should be read.
     */
    fun isWebvttHeaderLine(input: ParsableByteArray): Boolean {
        val line: String? = input.readLine()
        return line != null && line.startsWith(WEBVTT_HEADER)
    }

    /**
     * Parses a WebVTT timestamp.
     *
     * @param timestamp The timestamp string.
     * @return The parsed timestamp in microseconds.
     * @throws NumberFormatException If the timestamp could not be parsed.
     */
    @Throws(java.lang.NumberFormatException::class)
    fun parseTimestampUs(timestamp: String): Long {
        var value: Long = 0
        val parts: Array<String> = splitAtFirst(timestamp, "\\.")
        val subparts: Array<String> = split(parts[0], ":")
        for (subpart in subparts) {
            value = (value * 60) + subpart.toLong()
        }
        value *= 1000
        if (parts.size == 2) {
            value += parts[1].toLong()
        }
        return value * 1000
    }

    /**
     * Parses a percentage string.
     *
     * @param s The percentage string.
     * @return The parsed value, where 1.0 represents 100%.
     * @throws NumberFormatException If the percentage could not be parsed.
     */
    @Throws(NumberFormatException::class)
    fun parsePercentage(s: String): Float {
        if (!s.endsWith("%")) {
            throw NumberFormatException("Percentages must end with %")
        }
        return s.substring(0, s.length - 1).toFloat() / 100
    }

    /**
     * Reads lines up to and including the next WebVTT cue header.
     *
     * @param input The input from which lines should be read.
     * @return A [Matcher] for the WebVTT cue header, or null if the end of the input was
     * reached without a cue header being found. In the case that a cue header is found, groups 1,
     * 2 and 3 of the returned matcher contain the start time, end time and settings list.
     */
    fun findNextCueHeader(input: ParsableByteArray): Matcher? {
        var line: String?
        while ((input.readLine().also { line = it }) != null) {
            if (COMMENT.matcher(line).matches()) {
                // Skip until the end of the comment block.
                while ((input.readLine().also { line = it }) != null && line?.isNotEmpty() == true) {
                }
            } else {
                val cueHeaderMatcher: Matcher = CUE_HEADER_PATTERN.matcher(line)
                if (cueHeaderMatcher.matches()) {
                    return cueHeaderMatcher
                }
            }
        }
        return null
    }
}

object LegacySubtitleUtil {
    /**
     * Converts a [Subtitle] to a list of [CuesWithTiming] representing it, emitted to
     * `output`.
     *
     *
     * This may only be called with [Subtitle] instances where the first event is non-empty
     * and the last event is an empty cue list.
     */
    fun toCuesWithTiming(
        subtitle: Subtitle, outputOptions: OutputOptions, output: Consumer<CuesWithTiming>
    ) {
        val startIndex = getStartIndex(subtitle, outputOptions)
        var startedInMiddleOfCue = false
        if (outputOptions.startTimeUs != TIME_UNSET) {
            val cuesAtStartTime: List<Cue> = subtitle.getCues(outputOptions.startTimeUs)
            val firstEventTimeUs: Long = subtitle.getEventTime(startIndex)
            if (!cuesAtStartTime.isEmpty() && startIndex < subtitle.getEventTimeCount() && outputOptions.startTimeUs < firstEventTimeUs) {
                output.accept(
                    CuesWithTiming(
                        cuesAtStartTime,
                        outputOptions.startTimeUs,
                        firstEventTimeUs - outputOptions.startTimeUs
                    )
                )
                startedInMiddleOfCue = true
            }
        }
        for (i in startIndex until subtitle.getEventTimeCount()) {
            outputSubtitleEvent(subtitle, i, output)
        }
        if (outputOptions.outputAllCues) {
            val endIndex = if (startedInMiddleOfCue) startIndex - 1 else startIndex
            for (i in 0 until endIndex) {
                outputSubtitleEvent(subtitle, i, output)
            }
            if (startedInMiddleOfCue) {
                output.accept(
                    CuesWithTiming(
                        subtitle.getCues(outputOptions.startTimeUs),
                        subtitle.getEventTime(endIndex),
                        outputOptions.startTimeUs - subtitle.getEventTime(endIndex)
                    )
                )
            }
        }
    }

    private fun getStartIndex(subtitle: Subtitle, outputOptions: OutputOptions): Int {
        if (outputOptions.startTimeUs === TIME_UNSET) {
            return 0
        }
        var nextEventTimeIndex: Int = subtitle.getNextEventTimeIndex(outputOptions.startTimeUs)
        if (nextEventTimeIndex == INDEX_UNSET) {
            return subtitle.getEventTimeCount()
        }
        if (nextEventTimeIndex > 0
            && subtitle.getEventTime(nextEventTimeIndex - 1) === outputOptions.startTimeUs
        ) {
            nextEventTimeIndex--
        }
        return nextEventTimeIndex
    }

    private fun outputSubtitleEvent(
        subtitle: Subtitle, eventIndex: Int, output: Consumer<CuesWithTiming>
    ) {
        val startTimeUs: Long = subtitle.getEventTime(eventIndex)
        val cuesForThisStartTime: List<Cue> = subtitle.getCues(startTimeUs)
        if (cuesForThisStartTime.isEmpty()) {
            // An empty cue list has already been implicitly encoded in the duration of the previous
            // sample.
            return
        } else // The last cue list must be empty
            check(eventIndex != subtitle.getEventTimeCount() - 1)
        // It's safe to inspect element i+1, because we already exited the loop above if
        // i == getEventTimeCount() - 1.
        val durationUs: Long = subtitle.getEventTime(eventIndex + 1) - subtitle.getEventTime(eventIndex)
        output.accept(CuesWithTiming(cuesForThisStartTime, startTimeUs, durationUs))
    }
}

class OutputOptions private constructor(
    /**
     * Cues after this time (inclusive) will be emitted first. Cues before this time might be
     * emitted later, depending on [.outputAllCues]. Can be [C.TIME_UNSET] to emit all
     * cues.
     */
    val startTimeUs: Long,
    /**
     * Whether to eventually emit all cues, or only those after [.startTimeUs]. Ignored if
     * [.startTimeUs] is not set.
     */
    val outputAllCues: Boolean,

    /**
     * For each cue, include all cues which occur between the start and end timestamp
     */
    val groupCuesInRange: Boolean
) {
    companion object {
        private val ALL = OutputOptions(TIME_UNSET,  /* outputAllCues= */false, true)
        private val ALL_UNGROUPED = OutputOptions(TIME_UNSET, false, false)

        /** Output all [CuesWithTiming] instances.  */
        fun allCues(): OutputOptions {
            return ALL
        }

        /**
         * Output all [CuesWithTiming] instances.
         * Each cue only contains the cue at the [CuesWithTiming.startTimeUs] and [CuesWithTiming.endTimeUs]
         */
        fun allCuesUngrouped(): OutputOptions {
            return ALL_UNGROUPED
        }

        /**
         * Only output [CuesWithTiming] instances where [CuesWithTiming.startTimeUs] is at
         * least `startTimeUs`.
         *
         *
         * The order in which [CuesWithTiming] instances are emitted is not defined.
         */
        fun onlyCuesAfter(startTimeUs: Long): OutputOptions {
            return OutputOptions(startTimeUs,  /* outputAllCues= */false, true)
        }

        /**
         * Output [CuesWithTiming] where [CuesWithTiming.startTimeUs] is at least `startTimeUs`, followed by the remaining [CuesWithTiming] instances.
         *
         *
         * Beyond this, the order in which [CuesWithTiming] instances are emitted is not
         * defined.
         */
        fun cuesAfterThenRemainingCuesBefore(startTimeUs: Long): OutputOptions {
            return OutputOptions(startTimeUs,  /* outputAllCues= */true, true)
        }
    }
}